package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.BookDAO;
import com.market.dao.WishlistDAO;
import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 찜 — GET /wishlist 목록, POST /wishlist/toggle 찜하기·풀기 */
@WebServlet({ "/wishlist", "/wishlist/toggle" })
public class WishlistServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final WishlistDAO wishlistDAO = new WishlistDAO();
	private final BookDAO bookDAO = new BookDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setAttribute("books", wishlistDAO.findBooks(Web.member(req).getMemberNo()));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.render(req, resp, "wishlist");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		String id = req.getParameter("id");
		try {
			if (bookDAO.findById(id) != null) {
				boolean on = wishlistDAO.toggle(m.getMemberNo(), id);
				Web.flash(req, on ? "찜한 책에 넣었어요" : "찜을 풀었어요");
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.redirect(req, resp, Web.safeNext(req.getParameter("next")));
	}
}
