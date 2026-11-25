package com.market.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 알림 한 건 */
public class Notification {
	private long id;
	private String message;
	private String link;
	private LocalDateTime createdAt;
	private boolean read;

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getLink() { return link; }
	public void setLink(String link) { this.link = link; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public boolean isRead() { return read; }
	public void setRead(boolean read) { this.read = read; }

	public String getCreatedAtText() {
		return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
	}
}
