package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
				"INSERT INTO member (login_id, password_hash, salt, name, phone, address, email, provider, provider_id, role) VALUES (?,?,?,?,?,?,?,?,?,?)",
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
}
