package com.market.api;

import java.sql.SQLException;

import com.market.dao.BookDAO;

/**
 * 도서 목록·검색의 입구.
 * - 베스트셀러·신간 탭: 우리 DB (판매량 순 / 출판일 순)
 * - 검색: 카카오 키가 있으면 카카오 책 검색 → 결과를 DB에 저장(캐시), 없거나 실패하면 DB 검색
 * DB에 저장해 두는 이유: 장바구니·주문·찜·리뷰가 book 테이블을 외래키로 참조하기 때문.
 */
public class BookCatalog {
	private final KakaoBookClient kakao = new KakaoBookClient();
	private final BookDAO bookDAO = new BookDAO();

	public boolean isApiOn() { return kakao.isConfigured(); }

	/**
	 * @param tab  bestseller / new (검색어가 없을 때)
	 * @param q    검색어 (있으면 검색)
	 * @param sort accuracy / latest / sales (sales는 DB 검색에서만)
	 */
	public BookPage browse(String tab, String q, String sort, int page) throws SQLException {
		page = Math.max(1, page);
		boolean searching = q != null && !q.isBlank();
		if (searching && kakao.isConfigured() && !"sales".equals(sort)) {
			try {
				BookPage p = kakao.search(q.trim(), sort, page);
				bookDAO.saveFromApi(p.getBooks());
				return p;
			} catch (Exception e) {
				if (e instanceof InterruptedException)
					Thread.currentThread().interrupt();
				BookPage local = bookDAO.browse(tab, q, sort, page, kakao.pageSize());
				local.setNotice("카카오 책 검색에 연결하지 못해서 저장된 도서에서 찾았어요. 잠시 뒤 다시 시도해 주세요.");
				return local;
			}
		}
		return bookDAO.browse(tab, q, sort, page, kakao.pageSize());
	}
}
