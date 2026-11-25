package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.market.api.BookCatalog;
import com.market.dao.BookDAO;
import com.market.dao.OrderDAO;
import com.market.dao.ReviewDAO;
import com.market.dao.WishlistDAO;
import com.market.dto.Book;
import com.market.dto.Member;
import com.market.dto.Review;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 도서 — 원본 CartAddItemPage
 * GET  /books                 베스트셀러·신간 탭(DB), 검색(q, 카카오), 정렬(sort), 페이지(page)
 * GET  /books/detail?id=      상세 + 리뷰 + 찜 상태 (최근 본 책에 추가)
 * POST /books/review          리뷰 쓰기 (구매한 회원만)
 * POST /books/review/delete   리뷰 지우기 (본인·관리자)
 */
@WebServlet({ "/books", "/books/detail", "/books/review", "/books/review/delete" })
public class BookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int RECENT_MAX = 5;
	private final BookCatalog catalog = new BookCatalog();
	private final BookDAO bookDAO = new BookDAO();
	private final ReviewDAO reviewDAO = new ReviewDAO();
	private final WishlistDAO wishlistDAO = new WishlistDAO();
	private final OrderDAO orderDAO = new OrderDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (req.getServletPath().equals("/books/detail")) {
				detail(req, resp);
				return;
			}
			if (!req.getServletPath().equals("/books")) {
				resp.sendError(405);
				return;
			}
			String q = Web.trim(req.getParameter("q"));
			String tab = "new".equals(req.getParameter("tab")) ? "new" : "bestseller";
			String sort = Web.trim(req.getParameter("sort"));
			if (!sort.equals("latest") && !sort.equals("sales"))
				sort = "accuracy";
			int page = Math.max(1, Web.toInt(req.getParameter("page"), 1));
			req.setAttribute("q", q);
			req.setAttribute("tab", tab);
			req.setAttribute("sort", sort);
			req.setAttribute("apiOn", catalog.isApiOn());
			com.market.dto.Genre genre = q.isEmpty() ? com.market.dto.Genre.bySlug(req.getParameter("genre")) : null;
			req.setAttribute("genre", genre);
			req.setAttribute("genres", com.market.dto.Genre.values());
			com.market.api.BookPage result = genre != null ? catalog.browseGenre(genre, tab, page) : catalog.browse(tab, q, sort, page);
			req.setAttribute("result", result);
			req.setAttribute("genreCounts", bookDAO.countByGenre());
			java.util.List<String> pop = new com.market.dao.SearchDAO().popular();
			if (!pop.isEmpty())
				req.setAttribute("popularKeywords", pop);
			if (!q.isEmpty() && page == 1)
				SearchServlet.remember(req, q, result.getTotal());
			if (q.isEmpty() && genre == null && page == 1 && tab.equals("bestseller"))
				home(req);
			Web.render(req, resp, "books");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	/** 첫 화면: 윤상문고 추천 + 이번 주 베스트 10(지난주 대비 순위 변화) */
	private void home(HttpServletRequest req) throws SQLException {
		req.setAttribute("home", true);
		req.setAttribute("pick", new com.market.dao.PickDAO().latest());
		com.market.dto.Genre wk = com.market.dto.Genre.bySlug(req.getParameter("wk"));
		req.setAttribute("wkGenre", wk);
		req.setAttribute("weekly", weeklyBest(wk == null ? null : wk.getLabel()));
	}

	/** 최근 7일 판매 순위 10권. 그 전 7일의 순위와 비교해 rankDelta(+상승/-하락/null 새로 들어옴) */
	List<Book> weeklyBest(String genre) throws SQLException {
		java.time.LocalDateTime now = com.market.util.Clock.now().plusSeconds(1);
		List<Book> thisWeek = bookDAO.bestBetween(now.minusDays(7), now, genre, 10);
		List<Book> lastWeek = bookDAO.bestBetween(now.minusDays(14), now.minusDays(7), genre, 100);
		java.util.Map<String, Integer> lastRank = new java.util.HashMap<>();
		for (int i = 0; i < lastWeek.size(); i++)
			lastRank.put(lastWeek.get(i).getBookId(), i + 1);
		for (int i = 0; i < thisWeek.size(); i++) {
			Book b = thisWeek.get(i);
			b.setRank(i + 1);
			Integer before = lastRank.get(b.getBookId());
			b.setRankDelta(before == null ? null : before - (i + 1));
		}
		return thisWeek;
	}

	private void detail(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		Book book = bookDAO.findById(req.getParameter("id"));
		if (book == null) {
			resp.sendError(404, "찾는 도서가 없어요.");
			return;
		}
		remember(req.getSession(), book);
		List<Review> reviews = reviewDAO.findByBook(book.getBookId());
		double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
		req.setAttribute("book", book);
		req.setAttribute("reviews", reviews);
		req.setAttribute("avgRating", Math.round(avg * 10) / 10.0);
		req.setAttribute("sameGenre", bookDAO.sameGenre(book.getCategory(), book.getBookId(), 4));
		req.setAttribute("together", bookDAO.boughtTogether(book.getBookId(), 4));

		Member m = Web.member(req);
		if (m != null) {
			req.setAttribute("wished", wishlistDAO.contains(m.getMemberNo(), book.getBookId()));
			if (book.isSoldOut())
				req.setAttribute("restockRequested", new com.market.dao.RestockDAO().has(m.getMemberNo(), book.getBookId()));
			boolean reviewed = reviews.stream().anyMatch(r -> r.getMemberNo() == m.getMemberNo());
			req.setAttribute("reviewed", reviewed);
			req.setAttribute("canReview", !reviewed && orderDAO.hasPurchased(m.getMemberNo(), book.getBookId()));
		}
		Web.render(req, resp, "book-detail");
	}

	/** 최근 본 책: 세션에 최대 5권, 방금 본 책이 맨 앞 */
	@SuppressWarnings("unchecked")
	private void remember(HttpSession session, Book book) {
		LinkedList<Book> recent = (LinkedList<Book>) session.getAttribute("recent");
		if (recent == null)
			recent = new LinkedList<>();
		recent.removeIf(b -> b.getBookId().equals(book.getBookId()));
		recent.addFirst(book);
		while (recent.size() > RECENT_MAX)
			recent.removeLast();
		session.setAttribute("recent", recent);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		String bookId = req.getParameter("id");
		try {
			if (req.getServletPath().equals("/books/review")) {
				int rating = Web.toInt(req.getParameter("rating"), 0);
				String content = Web.trim(req.getParameter("content"));
				if (!orderDAO.hasPurchased(m.getMemberNo(), bookId))
					Web.flash(req, "이 책을 구매한 회원만 리뷰를 쓸 수 있어요");
				else if (rating < 1 || rating > 5)
					Web.flash(req, "별점을 1~5개 중에서 골라 주세요");
				else if (content.isEmpty() || content.length() > 300)
					Web.flash(req, "리뷰는 1~300자로 써 주세요");
				else if (reviewDAO.insert(m.getMemberNo(), bookId, rating, content))
					Web.flash(req, "리뷰를 남겼어요");
				else
					Web.flash(req, "이 책에는 이미 리뷰를 남겼어요");
			} else if (req.getServletPath().equals("/books/review/delete")) {
				long reviewId = Web.toInt(req.getParameter("reviewId"), -1);
				if (reviewDAO.delete(reviewId, m.isAdmin() ? 0 : m.getMemberNo()))
					Web.flash(req, "리뷰를 지웠어요");
			} else {
				resp.sendError(405);
				return;
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.redirect(req, resp, "/books/detail?id=" + java.net.URLEncoder.encode(bookId, "UTF-8") + "#reviews");
	}
}
