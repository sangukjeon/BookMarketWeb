package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.BookDAO;
import com.market.dao.NotificationDAO;
import com.market.dao.RestockDAO;
import com.market.dto.Book;
import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 알림 · 재입고 알림 신청 (로그인 필요)
 * GET  /notifications   최근 알림 30개, 열면 모두 읽음으로
 * POST /restock         id, action=request|cancel — 품절 책 재입고 알림 신청·취소
 */
@WebServlet({ "/notifications", "/restock" })
public class NotificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final NotificationDAO notificationDAO = new NotificationDAO();
	private final RestockDAO restockDAO = new RestockDAO();
	private final BookDAO bookDAO = new BookDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getServletPath().equals("/notifications")) {
			resp.sendError(405);
			return;
		}
		Member m = Web.member(req);
		try {
			req.setAttribute("notifications", notificationDAO.findRecent(m.getMemberNo()));
			notificationDAO.markAllRead(m.getMemberNo());
			req.setAttribute("unreadCount", 0);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("nav", "notifications");
		Web.render(req, resp, "notifications");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getServletPath().equals("/restock")) {
			resp.sendError(405);
			return;
		}
		Member m = Web.member(req);
		String id = req.getParameter("id");
		try {
			Book b = bookDAO.findById(id);
			if (b == null) {
				resp.sendError(404, "찾는 도서가 없어요.");
				return;
			}
			if ("cancel".equals(req.getParameter("action"))) {
				restockDAO.cancel(m.getMemberNo(), b.getBookId());
				Web.flash(req, "재입고 알림을 취소했어요");
			} else if (!b.isSoldOut()) {
				Web.flash(req, "지금 재고가 있어서 바로 주문할 수 있어요");
			} else {
				restockDAO.request(m.getMemberNo(), b.getBookId());
				Web.flash(req, "다시 들어오면 알림으로 알려 드릴게요");
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.redirect(req, resp, "/books/detail?id=" + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8));
	}
}
