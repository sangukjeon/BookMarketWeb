package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import com.market.cart.Cart;
import com.market.cart.CartItem;
import com.market.util.DBUtil;

/**
 * 로그인한 회원의 장바구니 저장 (cart_item).
 * 화면에서 쓰는 장바구니는 여전히 세션의 Cart(원본 구조)이고, 바뀔 때마다 여기에 통째로 다시 저장한다.
 */
public class CartDAO {

	/** 저장된 장바구니: 책 번호 → 수량 (담은 순서대로) */
	public Map<String, Integer> load(long memberNo) throws SQLException {
		Map<String, Integer> map = new LinkedHashMap<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT book_id, quantity FROM cart_item WHERE member_no = ? ORDER BY updated_at, book_id")) {
			ps.setLong(1, memberNo);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					map.put(rs.getString("book_id"), rs.getInt("quantity"));
			}
		}
		return map;
	}

	/** 세션 장바구니를 그대로 저장 (지우고 다시 넣기, 트랜잭션) */
	public void save(long memberNo, Cart cart) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM cart_item WHERE member_no = ?")) {
					ps.setLong(1, memberNo);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement(
						"INSERT INTO cart_item (member_no, book_id, quantity, updated_at) VALUES (?,?,?,?)")) {
					LocalDateTime t = LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0);
					int i = 0;
					for (CartItem ci : cart.getItems()) {
						ps.setLong(1, memberNo);
						ps.setString(2, ci.getBookID());
						ps.setInt(3, ci.getQuantity());
						ps.setTimestamp(4, Timestamp.valueOf(t.plusSeconds(i++))); // 담은 순서 유지
						ps.addBatch();
					}
					if (!cart.isEmpty())
						ps.executeBatch();
				}
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
	}
}
