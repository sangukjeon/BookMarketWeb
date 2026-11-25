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
	private int stock = 20;

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

	/** 카카오(다음) 썸네일은 120x174라 흐려요. 주소의 크기 부분만 바꿔 가로 480 그림을 받아요. 다른 주소는 그대로. */
	public static final String HI_SIZE = "R480x0.q85";
	private static final java.util.regex.Pattern KAKAO_THUMB =
			java.util.regex.Pattern.compile("^(https?://[^/]*(?:kakaocdn\\.net|daumcdn\\.net)/thumb/)[A-Za-z]\\d+x\\d+(?:\\.q\\d+)?(/.*)$");

	public String getImageHi() {
		return hiRes(image);
	}

	/** 고해상도로 바꿀 수 있으면 true (화면에서 실패 시 원래 그림으로 되돌리는 데 씀) */
	public boolean isImageUpgraded() {
		return image != null && !image.equals(getImageHi());
	}

	static String hiRes(String url) {
		if (url == null)
			return null;
		java.util.regex.Matcher m = KAKAO_THUMB.matcher(url);
		return m.matches() ? m.group(1) + HI_SIZE + m.group(2) : url;
	}

	// ---------- 재고 ----------
	public int getStock() { return stock; }
	public void setStock(int stock) { this.stock = stock; }
	public boolean isSoldOut() { return stock <= 0; }
	/** 3권 이하로 남았을 때 "N권 남았어요" 안내 */
	public boolean isLowStock() { return stock > 0 && stock <= 3; }

	/** 장르 (태그 색·링크용). 장르 목록에 없는 값이면 null */
	public Genre getGenre() { return Genre.byLabel(getCategory()); }
}
