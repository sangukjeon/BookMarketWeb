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
}
