package com.market.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 윤상문고 추천 한 건 */
public class BookPick {
	private long pickId;
	private String bookId;
	private String comment;
	private LocalDateTime pickedAt;
	private Book book;

	public long getPickId() { return pickId; }
	public void setPickId(long pickId) { this.pickId = pickId; }
	public String getBookId() { return bookId; }
	public void setBookId(String bookId) { this.bookId = bookId; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	public LocalDateTime getPickedAt() { return pickedAt; }
	public void setPickedAt(LocalDateTime pickedAt) { this.pickedAt = pickedAt; }
	public Book getBook() { return book; }
	public void setBook(Book book) { this.book = book; }

	public String getPickedAtText() {
		return pickedAt == null ? "" : pickedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
	}
}
