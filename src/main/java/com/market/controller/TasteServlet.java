package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.dao.BookDAO;
import com.market.dao.CouponDAO;
import com.market.dao.NotificationDAO;
import com.market.dao.TasteDAO;
import com.market.dto.BearType;
import com.market.dto.Book;
import com.market.dto.Genre;
import com.market.dto.Member;
import com.market.dto.TasteQuiz;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 곰 독서 성향 테스트 (비회원도 가능, 보상 쿠폰은 회원만)
 * GET  /taste                 질문 5개
 * POST /taste                 q1~q5 → 곰 유형 계산 → (회원) 저장 + 첫 완료면 TASTE500
 * GET  /taste/result?type=    결과: 곰 설명 + 추천 장르 + 추천 책 3권
 */
@WebServlet({ "/taste", "/taste/result" })
public class TasteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String REWARD = "TASTE500";
	private final TasteDAO tasteDAO = new TasteDAO();
	private final BookDAO bookDAO = new BookDAO();
	private final CouponDAO couponDAO = new CouponDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("nav", "taste");
		try {
			if (req.getServletPath().equals("/taste/result")) {
				BearType t = BearType.bySlug(req.getParameter("type"));
				if (t == null) {
					Web.redirect(req, resp, "/taste");
					return;
				}
				req.setAttribute("bear", t);
				req.setAttribute("picks", recommend(t));
				Member m = Web.member(req);
				req.setAttribute("rewarded", m != null && couponDAO.inWallet(m.getMemberNo(), REWARD));
				Web.render(req, resp, "taste-result");
				return;
			}
			Member m = Web.member(req);
			if (m != null)
				req.setAttribute("myBear", tasteDAO.find(m.getMemberNo()));
			req.setAttribute("questions", TasteQuiz.QUESTIONS);
			Web.render(req, resp, "taste");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<String> answers = new ArrayList<>();
		for (int i = 1; i <= TasteQuiz.QUESTIONS.size(); i++)
			answers.add(Web.trim(req.getParameter("q" + i)));
		BearType t = TasteQuiz.score(answers);
		if (t == null) {
			req.setAttribute("error", "모든 질문에 하나씩 골라 주세요.");
			req.setAttribute("answers", answers);
			req.setAttribute("questions", TasteQuiz.QUESTIONS);
			req.setAttribute("nav", "taste");
			Web.render(req, resp, "taste");
			return;
		}
		Member m = Web.member(req);
		try {
			if (m != null) {
				tasteDAO.save(m.getMemberNo(), t);
				if (couponDAO.grantReward(m.getMemberNo(), REWARD)) {
					new NotificationDAO().add(m.getMemberNo(), "독서 성향 테스트 보상으로 500원 쿠폰이 쿠폰함에 들어왔어요", "/coupons");
					Web.flash(req, "500원 쿠폰을 쿠폰함에 넣었어요");
				}
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.redirect(req, resp, "/taste/result?type=" + t.getSlug());
	}

	/** 곰 유형의 장르에서 많이 팔린 책 3권, 모자라면 전체 베스트로 채움 */
	private List<Book> recommend(BearType t) throws SQLException {
		List<Book> list = new ArrayList<>();
		for (Genre g : t.getGenres())
			for (Book b : bookDAO.browse("bestseller", "", "accuracy", 1, 3, g.getLabel()).getBooks())
				if (list.size() < 3 && list.stream().noneMatch(x -> x.getBookId().equals(b.getBookId())))
					list.add(b);
		if (list.size() < 3)
			for (Book b : bookDAO.browse("bestseller", "", "accuracy", 1, 6).getBooks())
				if (list.size() < 3 && list.stream().noneMatch(x -> x.getBookId().equals(b.getBookId())))
					list.add(b);
		return list;
	}
}
