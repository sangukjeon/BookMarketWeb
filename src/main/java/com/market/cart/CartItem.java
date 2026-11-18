package com.market.cart;

import java.io.Serializable;

import com.market.dto.Book;

/** 장바구니 한 줄 (원본 CartItem 유지) */
public class CartItem implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int MAX_QUANTITY = 99;
	private Book itemBook;
	private String bookID;
	private int quantity;
	private int totalPrice;

	public CartItem(Book book) {
		this.itemBook = book;
		this.bookID = book.getBookId();
		this.quantity = 1;
		updateTotalPrice();
	}

	public Book getItemBook() { return itemBook; }
	public String getBookID() { return bookID; }
	public int getQuantity() { return quantity; }
	public int getTotalPrice() { return totalPrice; }

	public void setQuantity(int quantity) {
		this.quantity = Math.max(1, Math.min(MAX_QUANTITY, quantity));
		updateTotalPrice();
	}

	public void updateTotalPrice() {
		totalPrice = itemBook.getUnitPrice() * quantity;
	}
}
