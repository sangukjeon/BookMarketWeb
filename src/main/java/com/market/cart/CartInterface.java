package com.market.cart;

import com.market.dto.Book;

/** 원본 CartInterface를 웹용으로 정리 (콘솔 출력 메서드는 화면(JSP)이 대신한다) */
public interface CartInterface {
	boolean isCartInBook(String bookId);

	void insertBook(Book book);

	/** 새로 추가: 수량 줄이기. 1에서 줄이면 아무 일도 하지 않는다. */
	void decreaseQuantity(String bookId);

	void removeCart(String bookId);

	void deleteBook();
}
