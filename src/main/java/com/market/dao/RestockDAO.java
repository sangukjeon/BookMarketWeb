package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.util.Clock;
import com.market.util.DBUtil;

/** restock_request — 품절 책 재입고 알림 신청 */
public class RestockDAO {
	private final NotificationDAO notifications = new NotificationDAO();

	public boolean has(long memberNo, String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT 1 FROM restock_request WHERE member_no = ? AND book_id = ?")) {
			ps.setLong(1, memberNo);
			ps.setString(2, bookId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/** 신청 (이미 있으면 false) */
	public boolean request(long memberNo, String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO restock_request (member_no, book_id, requested_at) VALUES (?,?,?)")) {
			ps.setLong(1, memberNo);
			ps.setString(2, bookId);
			ps.setTimestamp(3, Clock.nowTs());
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			if (DBErrors.isConstraintViolation(e))
				return false;
			throw e;
		}
	}

	public void cancel(long memberNo, String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"DELETE FROM restock_request WHERE member_no = ? AND book_id = ?")) {
			ps.setLong(1, memberNo);
			ps.setString(2, bookId);
			ps.executeUpdate();
		}
	}

	public int countFor(String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT COUNT(*) FROM restock_request WHERE book_id = ?")) {
			ps.setString(1, bookId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		}
	}

	/**
	 * 책에 재고가 있으면 신청한 회원 모두에게 알림을 넣고 신청을 지운다 (한 트랜잭션).
	 * 관리자가 재고를 채웠을 때, 주문 취소로 재고가 돌아왔을 때 부른다. 보낸 알림 수를 돌려준다.
	 */
	public int notifyIfAvailable(String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				String name;
				try (PreparedStatement ps = con.prepareStatement("SELECT name, stock FROM book WHERE book_id = ?")) {
					ps.setString(1, bookId);
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next() || rs.getInt("stock") <= 0) {
							con.rollback();
							return 0;
						}
						name = rs.getString("name");
					}
				}
				List<Long> members = new ArrayList<>();
				try (PreparedStatement ps = con.prepareStatement("SELECT member_no FROM restock_request WHERE book_id = ?")) {
					ps.setString(1, bookId);
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next())
							members.add(rs.getLong(1));
					}
				}
				String title = name.length() > 60 ? name.substring(0, 60) + "…" : name;
				for (long m : members)
					notifications.add(con, m, "‘" + title + "’이(가) 다시 들어왔어요. 지금 주문할 수 있어요",
							"/books/detail?id=" + bookId);
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM restock_request WHERE book_id = ?")) {
					ps.setString(1, bookId);
					ps.executeUpdate();
				}
				con.commit();
				return members.size();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}
}
