package com.market.dto;

/**
 * 도서 장르. book.category에는 label(예: "소설")을 저장한다.
 * 카카오 책 검색은 장르 정보를 주지 않아서, 장르 화면에서는 keyword로 카카오를 검색해
 * 가져온 책에 그 장르를 붙여 저장한다 (관리자가 도서 수정 화면에서 고칠 수 있다).
 */
public enum Genre {
	NOVEL("novel", "소설", "장편소설", "rules"),
	ESSAY("essay", "시/에세이", "에세이", "tip"),
	HUMANITIES("humanities", "인문", "인문학", "class"),
	BUSINESS("business", "경제/경영", "경영", "study"),
	SELFDEV("selfdev", "자기계발", "자기계발", "tip"),
	SCIENCE("science", "과학", "교양 과학", "class"),
	IT("it", "IT/컴퓨터", "프로그래밍", "study"),
	KIDS("kids", "어린이", "동화", "rules"),
	COMICS("comics", "만화", "만화", "tip"),
	TRAVEL("travel", "여행", "여행 에세이", "class");

	private final String slug;
	private final String label;
	private final String keyword;
	private final String tone; // 태그 색 (components.css의 tag--*)

	Genre(String slug, String label, String keyword, String tone) {
		this.slug = slug;
		this.label = label;
		this.keyword = keyword;
		this.tone = tone;
	}

	public String getSlug() { return slug; }
	public String getLabel() { return label; }
	public String getKeyword() { return keyword; }
	public String getTone() { return tone; }

	public static Genre bySlug(String slug) {
		for (Genre g : values())
			if (g.slug.equals(slug))
				return g;
		return null;
	}

	public static Genre byLabel(String label) {
		for (Genre g : values())
			if (g.label.equals(label))
				return g;
		return null;
	}
}
