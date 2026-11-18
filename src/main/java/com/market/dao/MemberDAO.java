package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Member;
import com.market.util.DBUtil;
import com.market.util.PasswordUtil;

/** member 테이블 */
public class MemberDAO {

	private Member map(ResultSet rs) throws SQLException {
		Member m = new Member();
		m.setMemberNo(rs.getLong("member_no"));
		m.setLoginId(rs.getString("login_id"));
		m.setPasswordHash(rs.getString("password_hash"));
		m.setSalt(rs.getString("salt"));
		m.setName(rs.getString("name"));
		m.setPhone(rs.getString("phone"));
		m.setAddress(rs.getString("address"));
		m.setEmail(rs.getString("email"));
		m.setProvider(rs.getString("provider"));
		m.setProviderId(rs.getString("provider_id"));
		m.setRole(rs.getString("role"));
		m.setStatus(rs.getString("status"));
		Timestamp t = rs.getTimestamp("created_at");
		m.setCreatedAt(t == null ? null : t.toLocalDateTime());
		return m;
	}

	public Member findByLoginId(String loginId) throws SQLException {
		return findOne("SELECT * FROM member WHERE login_id = ?", loginId);
	}

	public Member findByNo(long memberNo) throws SQLException {
		return findOne("SELECT * FROM member WHERE member_no = ?", memberNo);
	}

	private Member findOne(String sql, Object... params) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	/** 자체 로그인. 아이디가 없거나 비밀번호가 틀리면 null */
	public Member login(String loginId, String password) throws SQLException {
		Member m = findByLoginId(loginId);
		if (m == null || m.isSocial())
			return null;
		return PasswordUtil.matches(password, m.getSalt(), m.getPasswordHash()) ? m : null;
	}

	/** 자체 회원가입. 비밀번호는 salt를 붙여 해시로만 저장한다 */
	public Member signup(String loginId, String password, String name, String phone, String address)
			throws SQLException {
		Member m = new Member();
		m.setLoginId(loginId);
		m.setSalt(PasswordUtil.newSalt());
		m.setPasswordHash(PasswordUtil.hash(password, m.getSalt()));
		m.setName(name);
		m.setPhone(phone);
		m.setAddress(address);
		insert(m);
		return m;
	}

	/** 소셜 로그인: (provider, providerId)로 찾고, 없으면 새로 가입시킨다 */
	public Member findOrCreateSocial(String provider, String providerId, String name, String email)
			throws SQLException {
		Member m = findOne("SELECT * FROM member WHERE provider = ? AND provider_id = ?", provider, providerId);
		if (m != null)
			return m;
		m = new Member();
		m.setProvider(provider);
		m.setProviderId(providerId);
		m.setLoginId(provider.toLowerCase() + "_" + providerId);
		m.setName(name);
		m.setEmail(email);
		insert(m);
		return m;
	}

	private void insert(Member m) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO member (login_id, password_hash, salt, name, phone, address, email, provider, provider_id, role, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
				new String[] { "member_no" })) { // MySQL·PostgreSQL 모두 자동 번호를 돌려받는다
			ps.setString(1, m.getLoginId());
			ps.setString(2, m.getPasswordHash());
			ps.setString(3, m.getSalt());
			ps.setString(4, m.getName());
			ps.setString(5, m.getPhone());
			ps.setString(6, m.getAddress());
			ps.setString(7, m.getEmail());
			ps.setString(8, m.getProvider());
			ps.setString(9, m.getProviderId());
			ps.setString(10, m.getRole());
			m.setCreatedAt(now()); // DB 서버 시간대와 상관없이 한국 시간으로
			ps.setTimestamp(11, Timestamp.valueOf(m.getCreatedAt()));
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					m.setMemberNo(rs.getLong(1));
			}
		}
	}

	public void updateProfile(Member m) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con
				.prepareStatement("UPDATE member SET name = ?, phone = ?, address = ? WHERE member_no = ?")) {
			ps.setString(1, m.getName());
			ps.setString(2, m.getPhone());
			ps.setString(3, m.getAddress());
			ps.setLong(4, m.getMemberNo());
			ps.executeUpdate();
		}
	}

	private static LocalDateTime now() {
		return LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0);
	}

	// ===================== 관리자: 회원 관리 =====================

	/** 회원 목록 + 회원별 주문 수·구매액(취소되지 않은 주문). 빈 값인 조건은 무시한다 */
	public List<Member> search(String q, String provider, String role, String status) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT m.*,"
				+ " (SELECT COUNT(*) FROM orders o WHERE o.member_no = m.member_no AND o.status <> 'CANCELLED') AS order_count,"
				+ " (SELECT COALESCE(SUM(o.pay_amount), 0) FROM orders o WHERE o.member_no = m.member_no AND o.status <> 'CANCELLED') AS total_spent"
				+ " FROM member m WHERE 1 = 1");
		List<Object> params = new ArrayList<>();
		if (q != null && !q.isBlank()) {
			sql.append(" AND (LOWER(m.name) LIKE ? OR LOWER(m.login_id) LIKE ? OR LOWER(m.email) LIKE ?)");
			String like = "%" + q.trim().toLowerCase() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (provider != null && !provider.isBlank()) {
			sql.append(" AND m.provider = ?");
			params.add(provider);
		}
		if (role != null && !role.isBlank()) {
			sql.append(" AND m.role = ?");
			params.add(role);
		}
		if (status != null && !status.isBlank()) {
			sql.append(" AND m.status = ?");
			params.add(status);
		}
		sql.append(" ORDER BY m.member_no DESC");
		List<Member> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++)
				ps.setObject(i + 1, params.get(i));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Member m = map(rs);
					m.setOrderCount(rs.getInt("order_count"));
					m.setTotalSpent(rs.getLong("total_spent"));
					list.add(m);
				}
			}
		}
		return list;
	}

	/** 관리자 대시보드용 숫자: [전체 회원, 오늘 가입, 정지] */
	public int[] counts() throws SQLException {
		Timestamp today = Timestamp.valueOf(LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay());
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT COUNT(*) AS total,"
						+ " COALESCE(SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END), 0) AS today,"
						+ " COALESCE(SUM(CASE WHEN status = 'SUSPENDED' THEN 1 ELSE 0 END), 0) AS suspended FROM member")) {
			ps.setTimestamp(1, today);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return new int[] { rs.getInt("total"), rs.getInt("today"), rs.getInt("suspended") };
			}
		}
	}

	/** 계정 정지(SUSPENDED) / 해제(ACTIVE). 바뀌었으면 true */
	public boolean setStatus(long memberNo, String status) throws SQLException {
		return update("UPDATE member SET status = ? WHERE member_no = ? AND status <> ?", status, memberNo, status);
	}

	/** 권한 변경 USER / ADMIN. 바뀌었으면 true */
	public boolean setRole(long memberNo, String role) throws SQLException {
		return update("UPDATE member SET role = ? WHERE member_no = ? AND role <> ?", role, memberNo, role);
	}

	/** 자체 가입 회원의 비밀번호를 임시 비밀번호로 바꾸고 돌려준다. 소셜 회원이면 null */
	public String resetPassword(long memberNo) throws SQLException {
		String temp = PasswordUtil.tempPassword();
		String salt = PasswordUtil.newSalt();
		boolean ok = update("UPDATE member SET password_hash = ?, salt = ? WHERE member_no = ? AND provider = 'LOCAL'",
				PasswordUtil.hash(temp, salt), salt, memberNo);
		return ok ? temp : null;
	}

	private boolean update(String sql, Object... params) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			return ps.executeUpdate() > 0;
		}
	}
}
