package com.market.dto;

import java.io.Serializable;

/** 주문 항목 — 주문 당시의 도서명·가격을 그대로 남긴다 */
public class OrderItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private long orderId;
	private String bookId;
	private String bookName;
	private int unitPrice;
	private int quantity;
	private int lineTotal;

	public long getOrderId() { return orderId; }
	public void setOrderId(long orderId) { this.orderId = orderId; }
	public String getBookId() { return bookId; }
	public void setBookId(String bookId) { this.bookId = bookId; }
	public String getBookName() { return bookName; }
	public void setBookName(String bookName) { this.bookName = bookName; }
	public int getUnitPrice() { return unitPrice; }
	public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public int getLineTotal() { return lineTotal; }
	public void setLineTotal(int lineTotal) { this.lineTotal = lineTotal; }
}
