package com.market.dto;

import java.io.Serializable;

/** 상품 공통 부모 클래스 (원본 com.market.bookitem.Item 유지) */
public abstract class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String bookId;
	protected String name;
	protected int unitPrice;

	protected Item() {
	}

	protected Item(String bookId, String name, int unitPrice) {
		this.bookId = bookId;
		this.name = name;
		this.unitPrice = unitPrice;
	}

	public abstract String getBookId();

	public abstract String getName();

	public abstract int getUnitPrice();
}
