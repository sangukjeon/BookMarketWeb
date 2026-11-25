package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Coupon;
import com.market.util.DBUtil;

/** coupon 테이블 */
public class CouponDAO {

	private Coupon map(ResultSet rs) throws SQLException {
		Coupon c = new Coupon();
		c.setCode(rs.getString("code"));
		c.setName(rs.getString("name"));
		c.setType(rs.getString("type"));
		c.setValue(rs.getInt("value"));
		c.setMinAmount(rs.getInt("min_amount"));
		int max = rs.getInt("max_discount");
		c.setMaxDiscount(rs.wasNull() ? null : max);
		c.setActive(rs.getBoolean("active"));
		return c;
	}

	public Coupon findByCode(String code) throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT * FROM coupon WHERE code = ?")) {
			ps.setString(1, code.trim().toUpperCase());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	public List<Coupon> findAll() throws SQLException {
		return list("SELECT * FROM coupon ORDER BY active DESC, code");
	}

	public List<Coupon> findActive() throws SQLException {
		return list("SELECT * FROM coupon WHERE active = TRUE ORDER BY code");
	}

	private List<Coupon> list(String sql) throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			List<Coupon> list = new ArrayList<>();
			while (rs.next())
				list.add(map(rs));
			return list;
		}
	}

	public void insert(Coupon c) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO coupon (code, name, type, value, min_amount, max_discount, active) VALUES (?,?,?,?,?,?,TRUE)")) {
			ps.setString(1, c.getCode().trim().toUpperCase());
			ps.setString(2, c.getName());
			ps.setString(3, c.getType());
			ps.setInt(4, c.getValue());
			ps.setInt(5, c.getMinAmount());
			ps.setObject(6, c.getMaxDiscount());
			ps.executeUpdate();
		}
	}

	public void toggleActive(String code) throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE coupon SET active = NOT active WHERE code = ?")) {
			ps.setString(1, code);
			ps.executeUpdate();
		}
	}

	// ===================== 회원 쿠폰함 (member_coupon) =====================

	public enum RegisterResult { OK, NOT_FOUND, INACTIVE, ALREADY }

	/** 쿠폰 번호 등록. 없는 번호·멈춘 쿠폰·이미 등록한 쿠폰은 등록하지 않는다 */
	public RegisterResult register(long memberNo, String code) throws SQLException {
		Coupon c = code == null || code.isBlank() ? null : findByCode(code);
		if (c == null)
			return RegisterResult.NOT_FOUND;
		if (!c.isActive())
			return RegisterResult.INACTIVE;
		if (inWallet(memberNo, c.getCode()))
			return RegisterResult.ALREADY;
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO member_coupon (member_no, code, registered_at) VALUES (?,?,?)")) {
			ps.setLong(1, memberNo);
			ps.setString(2, c.getCode());
			ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul")).withNano(0)));
			ps.executeUpdate();
			return RegisterResult.OK;
		} catch (SQLException e) {
			if (DBErrors.isConstraintViolation(e)) // 두 번 눌러서 동시에 들어온 경우
				return RegisterResult.ALREADY;
			throw e;
		}
	}

	/** 가입 선물처럼 조용히 넣어 줄 때 (쿠폰이 없거나 멈췄으면 아무것도 안 함) */
	public void grant(long memberNo, String code) throws SQLException {
		register(memberNo, code);
	}

	public boolean inWallet(long memberNo, String code) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT 1 FROM member_coupon WHERE member_no = ? AND code = ?")) {
			ps.setLong(1, memberNo);
			ps.setString(2, code == null ? "" : code.trim().toUpperCase());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/** 내 쿠폰함 (최근 등록 순). used = 취소되지 않은 주문에 이미 씀 */
	public List<Coupon> findWallet(long memberNo) throws SQLException {
		List<Coupon> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT c.*, mc.registered_at, "
						+ "(SELECT COUNT(*) FROM orders o WHERE o.member_no = mc.member_no AND o.coupon_code = c.code AND o.status <> 'CANCELLED') AS used_cnt "
						+ "FROM member_coupon mc JOIN coupon c ON c.code = mc.code WHERE mc.member_no = ? ORDER BY mc.registered_at DESC, c.code")) {
			ps.setLong(1, memberNo);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Coupon c = map(rs);
					java.sql.Timestamp t = rs.getTimestamp("registered_at");
					c.setRegisteredAt(t == null ? null : t.toLocalDateTime());
					c.setUsed(rs.getInt("used_cnt") > 0);
					list.add(c);
				}
			}
		}
		return list;
	}

	/** 관리자 쿠폰 화면: 쿠폰마다 등록한 회원 수 / 사용 횟수 */
	public List<Coupon> findAllWithStats() throws SQLException {
		List<Coupon> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT c.*, (SELECT COUNT(*) FROM member_coupon mc WHERE mc.code = c.code) AS reg_cnt, "
						+ "(SELECT COUNT(*) FROM orders o WHERE o.coupon_code = c.code AND o.status <> 'CANCELLED') AS used_cnt "
						+ "FROM coupon c ORDER BY c.active DESC, c.code");
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Coupon c = map(rs);
				c.setRegisteredCount(rs.getInt("reg_cnt"));
				c.setUsedCount(rs.getInt("used_cnt"));
				list.add(c);
			}
		}
		return list;
	}
}
