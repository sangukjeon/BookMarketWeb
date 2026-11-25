package com.market.dao;

import java.sql.SQLException;

/** 주문하는 사이에 재고가 모자라게 된 경우. 주문 저장 트랜잭션은 모두 되돌려진다 */
public class OutOfStockException extends SQLException {
	private static final long serialVersionUID = 1L;
	private final String bookName;

	public OutOfStockException(String bookName) {
		super("재고 부족: " + bookName);
		this.bookName = bookName;
	}

	public String getBookName() { return bookName; }
}
