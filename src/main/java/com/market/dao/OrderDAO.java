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

import com.market.dto.Order;
import com.market.dto.OrderItem;
import com.market.util.DBUtil;

/** orders + order_item 테이블. 주문 저장은 트랜잭션으로 묶는다 */
public class OrderDAO {

	/** 주문 머리와 항목을 한 번에 저장한다. 하나라도 실패하면 모두 되돌린다 */
	public long insert(Order o) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				long orderId;
				try (PreparedStatement ps = con.prepareStatement(
						"INSERT INTO orders (member_no, receiver_name, receiver_phone, address, item_total, discount, shipping_fee, pay_amount, coupon_code, ordered_at) VALUES (?,?,?,?,?,?,?,?,?,?)",
						new String[] { "order_id" })) {
					ps.setLong(1, o.getMemberNo());
					ps.setString(2, o.getReceiverName());
					ps.setString(3, o.getReceiverPhone());
					ps.setString(4, o.getAddress());
					ps.setInt(5, o.getItemTotal());
					ps.setInt(6, o.getDiscount());
					ps.setInt(7, o.getShippingFee());
					ps.setInt(8, o.getPayAmount());
					ps.setString(9, o.getCouponCode());
					// DB 서버 시간대와 상관없이 한국 시간으로 저장
					o.setOrderedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0));
					ps.setTimestamp(10, Timestamp.valueOf(o.getOrderedAt()));
					ps.executeUpdate();
					try (ResultSet rs = ps.getGeneratedKeys()) {
						rs.next();
						orderId = rs.getLong(1);
					}
				}
				try (PreparedStatement ps = con.prepareStatement(
						"INSERT INTO order_item (order_id, book_id, book_name, unit_price, quantity, line_total) VALUES (?,?,?,?,?,?)")) {
					for (OrderItem it : o.getItems()) {
						ps.setLong(1, orderId);
						ps.setString(2, it.getBookId());
						ps.setString(3, it.getBookName());
						ps.setInt(4, it.getUnitPrice());
						ps.setInt(5, it.getQuantity());
						ps.setInt(6, it.getLineTotal());
						ps.addBatch();
					}
					ps.executeBatch();
				}
				con.commit();
				o.setOrderId(orderId);
				return orderId;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
	}

	private Order mapHead(ResultSet rs) throws SQLException {
		Order o = new Order();
		o.setOrderId(rs.getLong("order_id"));
		o.setMemberNo(rs.getLong("member_no"));
		o.setReceiverName(rs.getString("receiver_name"));
		o.setReceiverPhone(rs.getString("receiver_phone"));
		o.setAddress(rs.getString("address"));
		o.setItemTotal(rs.getInt("item_total"));
		o.setDiscount(rs.getInt("discount"));
		o.setShippingFee(rs.getInt("shipping_fee"));
		o.setPayAmount(rs.getInt("pay_amount"));
		o.setCouponCode(rs.getString("coupon_code"));
		o.setOrderedAt(rs.getTimestamp("ordered_at").toLocalDateTime());
		o.setStatus(rs.getString("status"));
		java.sql.Timestamp c = rs.getTimestamp("cancelled_at");
		o.setCancelledAt(c == null ? null : c.toLocalDateTime());
		java.sql.Timestamp sh = rs.getTimestamp("shipped_at");
		o.setShippedAt(sh == null ? null : sh.toLocalDateTime());
		java.sql.Timestamp dl = rs.getTimestamp("delivered_at");
		o.setDeliveredAt(dl == null ? null : dl.toLocalDateTime());
		return o;
	}

	/** 내 주문 목록 (최근 순). 항목 수와 첫 책 이름을 같이 가져온다 */
	public List<Order> findByMember(long memberNo) throws SQLException {
		return list("WHERE o.member_no = ?", memberNo);
	}

	/** 관리자: 전체 주문. status가 비어 있으면 전체 */
	public List<Order> findAll(String status) throws SQLException {
		if (status == null || status.isBlank())
			return list("");
		return list("WHERE o.status = ?", status);
	}

	private List<Order> list(String where, Object... params) throws SQLException {
		String sql = "SELECT o.*, m.name AS member_name, COUNT(i.book_id) AS item_count, MIN(i.book_name) AS first_name "
				+ "FROM orders o JOIN order_item i ON i.order_id = o.order_id JOIN member m ON m.member_no = o.member_no "
				+ where + " GROUP BY o.order_id, m.name ORDER BY o.order_id DESC";
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			List<Order> list = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Order o = mapHead(rs);
					o.setMemberName(rs.getString("member_name"));
					o.setItemCount(rs.getInt("item_count"));
					o.setFirstItemName(rs.getString("first_name"));
					list.add(o);
				}
			}
			return list;
		}
	}

	/**
	 * 관리자: 배송 단계를 한 칸 앞으로. 지금 상태가 from일 때만 바뀐다 (두 번 눌러도 한 번만, 뒤로 가기 불가).
	 * 배송 시작·완료 시각을 함께 남긴다.
	 * @return 바뀌었으면 true
	 */
	public boolean advance(long orderId, String from) throws SQLException {
		String to = Order.nextOf(from);
		if (to == null)
			return false;
		String timeCol = "SHIPPING".equals(to) ? ", shipped_at = ?" : "DELIVERED".equals(to) ? ", delivered_at = ?" : "";
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"UPDATE orders SET status = ?" + timeCol + " WHERE order_id = ? AND status = ?")) {
			int i = 1;
			ps.setString(i++, to);
			if (!timeCol.isEmpty())
				ps.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0)));
			ps.setLong(i++, orderId);
			ps.setString(i, from);
			return ps.executeUpdate() == 1;
		}
	}

	/** 관리자 주문 관리 칩에 보여 줄 상태별 주문 수 */
	public java.util.Map<String, Integer> countByStatus() throws SQLException {
		java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
		for (String st : new String[] { "ORDERED", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED" })
			map.put(st, 0);
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT status, COUNT(*) AS cnt FROM orders GROUP BY status");
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				map.put(rs.getString("status"), rs.getInt("cnt"));
		}
		return map;
	}

	/**
	 * 주문 취소. 배송이 시작되기 전(주문완료·배송 준비 중)만 취소할 수 있다.
	 * memberNo가 0이면 관리자(누구의 주문이든), 아니면 본인 주문만.
	 * @return 취소했으면 true
	 */
	public boolean cancel(long orderId, long memberNo) throws SQLException {
		String sql = "UPDATE orders SET status = 'CANCELLED', cancelled_at = ? WHERE order_id = ? AND status IN ('ORDERED', 'PREPARING')"
				+ (memberNo > 0 ? " AND member_no = ?" : "");
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0)));
			ps.setLong(2, orderId);
			if (memberNo > 0)
				ps.setLong(3, memberNo);
			return ps.executeUpdate() == 1;
		}
	}

	/** 이 쿠폰을 이미 쓴 주문이 (취소되지 않은 채로) 있는지 */
	public boolean hasUsedCoupon(long memberNo, String couponCode) throws SQLException {
		return exists("SELECT 1 FROM orders WHERE member_no = ? AND coupon_code = ? AND status <> 'CANCELLED'", memberNo,
				couponCode);
	}

	/** 이 책을 산 적이 있는지 (취소된 주문은 제외) — 리뷰 자격 */
	public boolean hasPurchased(long memberNo, String bookId) throws SQLException {
		return exists("SELECT 1 FROM orders o JOIN order_item i ON i.order_id = o.order_id "
				+ "WHERE o.member_no = ? AND i.book_id = ? AND o.status <> 'CANCELLED'", memberNo, bookId);
	}

	private boolean exists(String sql, Object... params) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	/** 관리자 매출 요약. 매출은 취소되지 않은 주문만 센다 */
	public SalesSummary summary() throws SQLException {
		SalesSummary s = new SalesSummary();
		LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate().atStartOfDay();
		try (Connection con = DBUtil.getConnection()) {
			try (PreparedStatement ps = con.prepareStatement("SELECT "
					+ "COALESCE(SUM(CASE WHEN status <> 'CANCELLED' THEN pay_amount END), 0) AS revenue, "
					+ "COUNT(CASE WHEN status <> 'CANCELLED' THEN 1 END) AS orders, "
					+ "COUNT(CASE WHEN status='CANCELLED' THEN 1 END) AS cancelled, "
					+ "COALESCE(SUM(CASE WHEN status <> 'CANCELLED' AND ordered_at >= ? THEN pay_amount END), 0) AS today_revenue, "
					+ "COUNT(CASE WHEN status <> 'CANCELLED' AND ordered_at >= ? THEN 1 END) AS today_orders, "
					+ "COALESCE(SUM(CASE WHEN status <> 'CANCELLED' THEN discount END), 0) AS discount "
					+ "FROM orders")) {
				ps.setTimestamp(1, Timestamp.valueOf(today));
				ps.setTimestamp(2, Timestamp.valueOf(today));
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					s.setRevenue(rs.getLong("revenue"));
					s.setOrders(rs.getInt("orders"));
					s.setCancelled(rs.getInt("cancelled"));
					s.setTodayRevenue(rs.getLong("today_revenue"));
					s.setTodayOrders(rs.getInt("today_orders"));
					s.setDiscount(rs.getLong("discount"));
				}
			}
			try (PreparedStatement ps = con.prepareStatement(
					"SELECT i.book_id, MAX(i.book_name) AS name, SUM(i.quantity) AS qty, SUM(i.line_total) AS amount "
							+ "FROM order_item i JOIN orders o ON o.order_id = i.order_id WHERE o.status <> 'CANCELLED' "
							+ "GROUP BY i.book_id ORDER BY qty DESC, amount DESC LIMIT 5");
					ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					s.getTopBooks().add(new SalesSummary.TopBook(rs.getString("book_id"), rs.getString("name"),
							rs.getInt("qty"), rs.getLong("amount")));
			}
		}
		return s;
	}

	/** 영수증 한 장. 다른 사람의 주문은 보이지 않게 memberNo도 같이 확인한다 (관리자는 0을 넘기면 전체) */
	public Order findReceipt(long orderId, long memberNo) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			Order o;
			String sql = "SELECT * FROM orders WHERE order_id = ?" + (memberNo > 0 ? " AND member_no = ?" : "");
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setLong(1, orderId);
				if (memberNo > 0)
					ps.setLong(2, memberNo);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next())
						return null;
					o = mapHead(rs);
				}
			}
			try (PreparedStatement ps = con
					.prepareStatement("SELECT * FROM order_item WHERE order_id = ? ORDER BY book_name")) {
				ps.setLong(1, orderId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						OrderItem it = new OrderItem();
						it.setOrderId(orderId);
						it.setBookId(rs.getString("book_id"));
						it.setBookName(rs.getString("book_name"));
						it.setUnitPrice(rs.getInt("unit_price"));
						it.setQuantity(rs.getInt("quantity"));
						it.setLineTotal(rs.getInt("line_total"));
						o.getItems().add(it);
					}
				}
			}
			o.setItemCount(o.getItems().size());
			return o;
		}
	}

	/** 하루 매출 (막대그래프용) */
	public record DayRevenue(String label, long amount, boolean today) {
		public String getLabel() { return label; }
		public long getAmount() { return amount; }
		public boolean isToday() { return today; }
	}

	/**
	 * 최근 days일의 날짜별 매출 (취소 제외, 한국 시간 기준).
	 * DB마다 날짜 함수가 달라서 SQL로 묶지 않고 자바에서 날짜별로 더한다.
	 */
	public List<DayRevenue> dailyRevenue(int days) throws SQLException {
		java.time.LocalDate today = LocalDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
		java.time.LocalDate from = today.minusDays(days - 1L);
		long[] sums = new long[days];
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT ordered_at, pay_amount FROM orders WHERE status <> 'CANCELLED' AND ordered_at >= ?")) {
			ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					long idx = java.time.temporal.ChronoUnit.DAYS.between(from,
							rs.getTimestamp("ordered_at").toLocalDateTime().toLocalDate());
					if (idx >= 0 && idx < days)
						sums[(int) idx] += rs.getLong("pay_amount");
				}
			}
		}
		List<DayRevenue> list = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			java.time.LocalDate d = from.plusDays(i);
			list.add(new DayRevenue(d.getMonthValue() + "/" + d.getDayOfMonth(), sums[i], d.equals(today)));
		}
		return list;
	}
}
