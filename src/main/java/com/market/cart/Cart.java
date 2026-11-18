package com.market.cart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Book;

/**
 * 장바구니. HttpSession에 사용자마다 하나씩 저장된다.
 * 원본은 public static int mCartCount 를 썼는데, 웹에서는 static 변수가
 * 모든 사용자에게 공유되므로 인스턴스 메서드 getCount()로 바꿨다.
 */
public class Cart implements CartInterface, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int FREE_SHIPPING_MIN = 30000;
	public static final int SHIPPING_FEE = 3000;

	private final List<CartItem> mCartItem = new ArrayList<>();

	public List<CartItem> getItems() { return mCartItem; }

	public boolean isEmpty() { return mCartItem.isEmpty(); }

	/** 담긴 책 권수 합계 (배지에 표시) */
	public int getCount() {
		int n = 0;
		for (CartItem item : mCartItem)
			n += item.getQuantity();
		return n;
	}

	public int getItemTotal() {
		int sum = 0;
		for (CartItem item : mCartItem)
			sum += item.getTotalPrice();
		return sum;
	}

	/** 3만 원 이상이면 배송비 무료 */
	public int shippingFeeFor(int amountAfterDiscount) {
		return mCartItem.isEmpty() || amountAfterDiscount >= FREE_SHIPPING_MIN ? 0 : SHIPPING_FEE;
	}

	private CartItem find(String bookId) {
		for (CartItem item : mCartItem)
			if (item.getBookID().equals(bookId))
				return item;
		return null;
	}

	@Override
	public boolean isCartInBook(String bookId) {
		CartItem item = find(bookId);
		if (item != null) {
			item.setQuantity(item.getQuantity() + 1);
			return true;
		}
		return false;
	}

	@Override
	public void insertBook(Book book) {
		if (!isCartInBook(book.getBookId()))
			mCartItem.add(new CartItem(book));
	}

	@Override
	public void decreaseQuantity(String bookId) {
		CartItem item = find(bookId);
		if (item != null && item.getQuantity() > 1)
			item.setQuantity(item.getQuantity() - 1);
	}

	@Override
	public void removeCart(String bookId) {
		mCartItem.removeIf(item -> item.getBookID().equals(bookId));
	}

	@Override
	public void deleteBook() {
		mCartItem.clear();
	}
}
