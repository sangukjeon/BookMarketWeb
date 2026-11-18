package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Book;
import com.market.util.DBUtil;

/** wishlist 테이블 (찜) */
public class WishlistDAO {

	public boolean contains(long memberNo, String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT 1 FROM wishlist WHERE member_no = ? AND book_id = ?")) {
			ps.setLong(1, memberNo);
			ps.setString(2, bookId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/** 찜 상태를 뒤집는다. 찜하게 되면 true */
	public boolean toggle(long memberNo, String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			try (PreparedStatement del = con.prepareStatement("DELETE FROM wishlist WHERE member_no = ? AND book_id = ?")) {
				del.setLong(1, memberNo);
				del.setString(2, bookId);
				if (del.executeUpdate() == 1)
					return false;
			}
			try (PreparedStatement ins = con.prepareStatement("INSERT INTO wishlist (member_no, book_id) VALUES (?, ?)")) {
				ins.setLong(1, memberNo);
				ins.setString(2, bookId);
				ins.executeUpdate();
				return true;
			}
		}
	}

	public List<Book> findBooks(long memberNo) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT b.* FROM wishlist w JOIN book b ON b.book_id = w.book_id WHERE w.member_no = ? ORDER BY w.created_at DESC")) {
			ps.setLong(1, memberNo);
			List<Book> list = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Book b = new Book(rs.getString("book_id"), rs.getString("name"), rs.getInt("unit_price"),
							rs.getString("author"), rs.getString("description"), rs.getString("category"),
							rs.getString("release_date"), rs.getString("image"));
					b.setPublisher(rs.getString("publisher"));
					list.add(b);
				}
			}
			return list;
		}
	}
}
