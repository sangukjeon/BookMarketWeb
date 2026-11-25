package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Notification;
import com.market.util.Clock;
import com.market.util.DBUtil;

/** notification — 재입고·배송·쿠폰 알림 (사이트 안에서만) */
public class NotificationDAO {
	public static final int LIST_MAX = 30;

	public void add(long memberNo, String message, String link) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			add(con, memberNo, message, link);
		}
	}

	/** 다른 트랜잭션 안에서 같이 넣을 때 */
	public void add(Connection con, long memberNo, String message, String link) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(
				"INSERT INTO notification (member_no, message, link, created_at) VALUES (?,?,?,?)")) {
			ps.setLong(1, memberNo);
			ps.setString(2, message.length() > 200 ? message.substring(0, 200) : message);
			ps.setString(3, link);
			ps.setTimestamp(4, Clock.nowTs());
			ps.executeUpdate();
		}
	}

	public int unreadCount(long memberNo) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT COUNT(*) FROM notification WHERE member_no = ? AND read_at IS NULL")) {
			ps.setLong(1, memberNo);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		}
	}

	public List<Notification> findRecent(long memberNo) throws SQLException {
		List<Notification> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT * FROM notification WHERE member_no = ? ORDER BY created_at DESC, notification_id DESC LIMIT ?")) {
			ps.setLong(1, memberNo);
			ps.setInt(2, LIST_MAX);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Notification n = new Notification();
					n.setId(rs.getLong("notification_id"));
					n.setMessage(rs.getString("message"));
					n.setLink(rs.getString("link"));
					n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					n.setRead(rs.getTimestamp("read_at") != null);
					list.add(n);
				}
			}
		}
		return list;
	}

	public void markAllRead(long memberNo) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"UPDATE notification SET read_at = ? WHERE member_no = ? AND read_at IS NULL")) {
			ps.setTimestamp(1, Clock.nowTs());
			ps.setLong(2, memberNo);
			ps.executeUpdate();
		}
	}
}
