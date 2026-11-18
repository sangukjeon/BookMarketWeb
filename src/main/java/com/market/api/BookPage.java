package com.market.api;

import java.util.ArrayList;
import java.util.List;

import com.market.dto.Book;

/** 도서 목록 한 페이지 (카카오든 DB든 같은 모양) */
public class BookPage {
	private List<Book> books = new ArrayList<>();
	private int page = 1;
	private int totalPages = 1;
	private int total;
	private String source = "LOCAL";
	private String notice;

	public boolean isHasPrev() { return page > 1; }
	public boolean isHasNext() { return page < totalPages; }

	public List<Book> getBooks() { return books; }
	public void setBooks(List<Book> books) { this.books = books; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = Math.max(1, totalPages); }
	public int getTotal() { return total; }
	public void setTotal(int total) { this.total = total; }
	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }
	public String getNotice() { return notice; }
	public void setNotice(String notice) { this.notice = notice; }
}
