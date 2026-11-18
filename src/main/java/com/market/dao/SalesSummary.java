package com.market.dao;

import java.util.ArrayList;
import java.util.List;

/** 관리자 매출 요약 */
public class SalesSummary {
	public record TopBook(String bookId, String name, int quantity, long amount) {
		public String getBookId() { return bookId; }
		public String getName() { return name; }
		public int getQuantity() { return quantity; }
		public long getAmount() { return amount; }
	}

	private long revenue;
	private int orders;
	private int cancelled;
	private long todayRevenue;
	private int todayOrders;
	private long discount;
	private final List<TopBook> topBooks = new ArrayList<>();

	/** 취소율 (%) */
	public int getCancelRate() {
		int all = orders + cancelled;
		return all == 0 ? 0 : Math.round(cancelled * 100f / all);
	}

	public long getRevenue() { return revenue; }
	public void setRevenue(long revenue) { this.revenue = revenue; }
	public int getOrders() { return orders; }
	public void setOrders(int orders) { this.orders = orders; }
	public int getCancelled() { return cancelled; }
	public void setCancelled(int cancelled) { this.cancelled = cancelled; }
	public long getTodayRevenue() { return todayRevenue; }
	public void setTodayRevenue(long todayRevenue) { this.todayRevenue = todayRevenue; }
	public int getTodayOrders() { return todayOrders; }
	public void setTodayOrders(int todayOrders) { this.todayOrders = todayOrders; }
	public long getDiscount() { return discount; }
	public void setDiscount(long discount) { this.discount = discount; }
	public List<TopBook> getTopBooks() { return topBooks; }
}
