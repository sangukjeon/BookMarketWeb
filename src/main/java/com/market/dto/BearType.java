package com.market.dto;

/**
 * 곰 독서 성향 테스트의 결과 다섯 가지. 질문의 보기마다 이 중 하나에 점수를 준다.
 * 그림은 images/mascot의 표정 그림을 돌려 쓴다.
 */
public enum BearType {
	ADVENTURE("adventure", "모험가 곰", "책 속에서 낯선 세계로 떠나는 걸 좋아해요. 긴 이야기에 푹 빠져 밤새 읽는 타입이에요.",
			"bear-wave.png", Genre.NOVEL, Genre.TRAVEL),
	THINKER("thinker", "생각하는 곰", "왜 그럴까 궁금한 게 많아요. 읽고 나서 오래 곱씹을 수 있는 책을 좋아해요.",
			"bear-search.png", Genre.HUMANITIES, Genre.SCIENCE),
	GROWER("grower", "성장하는 곰", "읽은 걸 바로 해 보는 실천파예요. 내일의 나를 바꿔 줄 책을 찾아요.",
			"bear-cheer.png", Genre.SELFDEV, Genre.BUSINESS),
	FEELER("feeler", "감성 곰", "짧은 문장 하나에도 마음이 움직여요. 그림과 글이 따뜻한 책이 잘 맞아요.",
			"bear-oops.png", Genre.ESSAY, Genre.COMICS),
	MAKER("maker", "만드는 곰", "직접 만들어 보며 배우는 걸 좋아해요. 따라 하며 결과가 보이는 책이 좋아요.",
			"bear.png", Genre.IT, Genre.KIDS);

	private final String slug;
	private final String label;
	private final String description;
	private final String image;
	private final Genre[] genres;

	BearType(String slug, String label, String description, String image, Genre... genres) {
		this.slug = slug;
		this.label = label;
		this.description = description;
		this.image = image;
		this.genres = genres;
	}

	public String getSlug() { return slug; }
	public String getLabel() { return label; }
	public String getDescription() { return description; }
	public String getImage() { return image; }
	public Genre[] getGenres() { return genres; }

	public static BearType bySlug(String slug) {
		for (BearType t : values())
			if (t.slug.equals(slug))
				return t;
		return null;
	}
}
