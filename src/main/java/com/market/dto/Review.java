package com.market.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 리뷰 (구매한 회원만, 책 1권에 1개) */
public class Review implements Serializable {
	private static final long serialVersionUID = 1L;
	private long reviewId;
	private long memberNo;
	private String memberName;
	private String bookId;
	private int rating;
	private String content;
	private LocalDateTime createdAt;

	/** 작성자 이름 가리기: 홍길동 → 홍*동, 김철 → 김* */
	public String getMaskedName() {
		if (memberName == null || memberName.length() < 2)
			return memberName;
		if (memberName.length() == 2)
			return memberName.charAt(0) + "*";
		return memberName.charAt(0) + "*".repeat(memberName.length() - 2) + memberName.charAt(memberName.length() - 1);
	}

	public String getCreatedAtText() {
		return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
	}

	public long getReviewId() { return reviewId; }
	public void setReviewId(long reviewId) { this.reviewId = reviewId; }
	public long getMemberNo() { return memberNo; }
	public void setMemberNo(long memberNo) { this.memberNo = memberNo; }
	public String getMemberName() { return memberName; }
	public void setMemberName(String memberName) { this.memberName = memberName; }
	public String getBookId() { return bookId; }
	public void setBookId(String bookId) { this.bookId = bookId; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
