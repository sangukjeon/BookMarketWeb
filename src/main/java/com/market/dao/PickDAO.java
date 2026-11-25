package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.BookPick;
import com.market.util.Clock;
import com.market.util.DBUtil;

/** book_pick — 윤상문고 추천 (가장 최근 것이 메인에) */
public class PickDAO {
	private final BookDAO bookDAO = new BookDAO();

	public void add(String bookId, String comment) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO book_pick (book_id, comment, picked_at) VALUES (?,?,?)")) {
			ps.setString(1, bookId);
			ps.setString(2, comment);
			ps.setTimestamp(3, Clock.nowTs());
			ps.executeUpdate();
		}
	}

	public boolean delete(long pickId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"DELETE FROM book_pick WHERE pick_id = ?")) {
			ps.setLong(1, pickId);
			return ps.executeUpdate() == 1;
		}
	}

	public BookPick latest() throws SQLException {
		List<BookPick> list = find(1);
		return list.isEmpty() ? null : list.get(0);
	}

	public List<BookPick> find(int limit) throws SQLException {
		List<BookPick> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT * FROM book_pick ORDER BY picked_at DESC, pick_id DESC LIMIT ?")) {
			ps.setInt(1, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					BookPick p = new BookPick();
					p.setPickId(rs.getLong("pick_id"));
					p.setBookId(rs.getString("book_id"));
					p.setComment(rs.getString("comment"));
					p.setPickedAt(rs.getTimestamp("picked_at").toLocalDateTime());
					list.add(p);
				}
			}
		}
		for (BookPick p : list)
			p.setBook(bookDAO.findById(p.getBookId()));
		return list;
	}
}
