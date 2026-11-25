package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import com.market.util.DBUtil;

/** attendance — 하루 한 번 출석 */
public class AttendanceDAO {
	public static final int GOAL_DAYS = 7;

	/** 출석 (오늘 이미 했으면 false) */
	public boolean check(long memberNo, LocalDate day) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO attendance (member_no, check_date) VALUES (?,?)")) {
			ps.setLong(1, memberNo);
			ps.setDate(2, java.sql.Date.valueOf(day));
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			if (DBErrors.isConstraintViolation(e))
				return false;
			throw e;
		}
	}

	/** from 이상 to 이하 출석한 날 */
	public Set<LocalDate> days(long memberNo, LocalDate from, LocalDate to) throws SQLException {
		Set<LocalDate> set = new TreeSet<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT check_date FROM attendance WHERE member_no = ? AND check_date >= ? AND check_date <= ?")) {
			ps.setLong(1, memberNo);
			ps.setDate(2, java.sql.Date.valueOf(from));
			ps.setDate(3, java.sql.Date.valueOf(to));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					set.add(rs.getDate(1).toLocalDate());
			}
		}
		return set;
	}

	/** 오늘(또는 오늘 아직 안 했으면 어제)까지 이어서 출석한 날 수 */
	public int streak(long memberNo, LocalDate today) throws SQLException {
		Set<LocalDate> set = days(memberNo, today.minusDays(400), today);
		LocalDate d = set.contains(today) ? today : today.minusDays(1);
		int n = 0;
		while (set.contains(d)) {
			n++;
			d = d.minusDays(1);
		}
		return n;
	}
}
