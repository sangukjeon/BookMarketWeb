package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Review;
import com.market.util.DBUtil;

/** review 테이블 */
public class ReviewDAO {

	public List<Review> findByBook(String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT r.*, m.name AS member_name FROM review r JOIN member m ON m.member_no = r.member_no "
						+ "WHERE r.book_id = ? ORDER BY r.review_id DESC")) {
			ps.setString(1, bookId);
			List<Review> list = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Review r = new Review();
					r.setReviewId(rs.getLong("review_id"));
					r.setMemberNo(rs.getLong("member_no"));
					r.setMemberName(rs.getString("member_name"));
					r.setBookId(rs.getString("book_id"));
					r.setRating(rs.getInt("rating"));
					r.setContent(rs.getString("content"));
					r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
					list.add(r);
				}
			}
			return list;
		}
	}

	/** 같은 책에 이미 리뷰가 있으면 false */
	public boolean insert(long memberNo, String bookId, int rating, String content) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO review (member_no, book_id, rating, content, created_at) VALUES (?,?,?,?,?)")) {
			ps.setLong(1, memberNo);
			ps.setString(2, bookId);
			ps.setInt(3, rating);
			ps.setString(4, content);
			ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0)));
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			if (DBErrors.isConstraintViolation(e))
				return false; // 같은 책에 이미 리뷰가 있음 (uk_review_once)
			throw e;
		}
	}

	/** 본인 리뷰만 지운다 (관리자는 memberNo 0) */
	public boolean delete(long reviewId, long memberNo) throws SQLException {
		String sql = "DELETE FROM review WHERE review_id = ?" + (memberNo > 0 ? " AND member_no = ?" : "");
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, reviewId);
			if (memberNo > 0)
				ps.setLong(2, memberNo);
			return ps.executeUpdate() == 1;
		}
	}
}
