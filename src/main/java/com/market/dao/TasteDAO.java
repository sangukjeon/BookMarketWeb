package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.market.dto.BearType;
import com.market.util.Clock;
import com.market.util.DBUtil;

/** taste_result — 회원당 하나 (다시 하면 지우고 넣는다) */
public class TasteDAO {
	public void save(long memberNo, BearType type) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM taste_result WHERE member_no = ?")) {
					ps.setLong(1, memberNo);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement(
						"INSERT INTO taste_result (member_no, bear_type, taken_at) VALUES (?,?,?)")) {
					ps.setLong(1, memberNo);
					ps.setString(2, type.getSlug());
					ps.setTimestamp(3, Clock.nowTs());
					ps.executeUpdate();
				}
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	public BearType find(long memberNo) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT bear_type FROM taste_result WHERE member_no = ?")) {
			ps.setLong(1, memberNo);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? BearType.bySlug(rs.getString(1)) : null;
			}
		}
	}
}
