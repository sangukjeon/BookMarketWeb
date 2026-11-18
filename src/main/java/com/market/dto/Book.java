package com.market.dto;

/** 도서 (Item 상속) — book 테이블 한 줄 */
public class Book extends Item {
	private static final long serialVersionUID = 1L;
	private String author;
	private String description;
	private String category;
	private String releaseDate;
	private String image;
	private String publisher;
	private String source = "LOCAL";

	public Book() {
	}

	public Book(String bookId, String name, int unitPrice, String author, String description, String category,
			String releaseDate, String image) {
		super(bookId, name, unitPrice);
		this.author = author;
		this.description = description;
		this.category = category;
		this.releaseDate = releaseDate;
		this.image = image;
	}

	@Override
	public String getBookId() { return bookId; }
	public void setBookId(String bookId) { this.bookId = bookId; }
	@Override
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	@Override
	public int getUnitPrice() { return unitPrice; }
	public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public String getReleaseDate() { return releaseDate; }
	public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
	public String getImage() { return image; }
	public void setImage(String image) { this.image = image; }
	public String getPublisher() { return publisher; }
	public void setPublisher(String publisher) { this.publisher = publisher; }
	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }
	/** 표지가 인터넷 주소(카카오 등)인지, images/books 안의 파일인지 */
	public boolean isRemoteImage() { return image != null && image.startsWith("http"); }
}
