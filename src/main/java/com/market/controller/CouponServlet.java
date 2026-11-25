package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.CouponDAO;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 내 쿠폰함
 * GET  /coupons   등록한 쿠폰 목록 (사용 가능 / 사용함 / 멈춤)
 * POST /coupons   쿠폰 번호 등록
 */
@WebServlet("/coupons")
public class CouponServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final CouponDAO couponDAO = new CouponDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		render(req, resp);
	}

	private void render(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setAttribute("wallet", couponDAO.findWallet(Web.member(req).getMemberNo()));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		String next = req.getParameter("next");
		req.setAttribute("next", next == null ? "" : Web.safeNext(next));
		Web.render(req, resp, "coupons");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String code = Web.trim(req.getParameter("code")).toUpperCase();
		if (CouponDAO.REWARD_ONLY.contains(code)) {
			req.setAttribute("code", code);
			req.setAttribute("error", "이 쿠폰은 독서 성향 테스트나 7일 출석으로 받을 수 있어요.");
			render(req, resp);
			return;
		}
		CouponDAO.RegisterResult r;
		try {
			r = couponDAO.register(Web.member(req).getMemberNo(), code);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		if (r != CouponDAO.RegisterResult.OK) {
			req.setAttribute("code", code);
			req.setAttribute("error", switch (r) {
			case NOT_FOUND -> "없는 쿠폰 번호예요. 대소문자와 숫자를 다시 확인해 주세요.";
			case INACTIVE -> "지금은 쓸 수 없는 쿠폰이에요.";
			default -> "이미 쿠폰함에 있는 쿠폰이에요.";
			});
			render(req, resp);
			return;
		}
		Web.flash(req, "쿠폰을 쿠폰함에 넣었어요");
		String next = req.getParameter("next");
		Web.redirect(req, resp, next == null || next.isBlank() ? "/coupons" : Web.safeNext(next));
	}
}
