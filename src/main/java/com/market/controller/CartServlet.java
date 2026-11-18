package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.cart.Cart;
import com.market.dao.BookDAO;
import com.market.dto.Book;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 장바구니 — 원본 MainWindow의 버튼 2~6번
 * GET  /cart          목록 보기
 * POST /cart/add      담기 (bt4)
 * POST /cart/increase 수량 늘리기
 * POST /cart/decrease 수량 줄이기 (bt5, 원본에는 기능이 없었음)
 * POST /cart/remove   항목 삭제 (bt6)
 * POST /cart/clear    비우기 (bt3)
 */
@WebServlet({ "/cart", "/cart/*" })
public class CartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final BookDAO bookDAO = new BookDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Web.cart(req);
		Web.render(req, resp, "cart");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Cart cart = Web.cart(req);
		String action = req.getPathInfo() == null ? "" : req.getPathInfo();
		String id = req.getParameter("id");
		try {
			switch (action) {
			case "/add": {
				Book book = bookDAO.findById(id);
				if (book != null) {
					cart.insertBook(book);
					Web.flash(req, "장바구니에 담았어요");
				}
				// 상세 화면에서 "바로 구매"를 누르면 장바구니로, 아니면 원래 화면으로
				if ("buy".equals(req.getParameter("then"))) {
					Web.redirect(req, resp, "/cart");
				} else {
					Web.redirect(req, resp, Web.safeNext(req.getParameter("next")));
				}
				return;
			}
			case "/increase":
				cart.isCartInBook(id);
				break;
			case "/decrease":
				cart.decreaseQuantity(id);
				break;
			case "/remove":
				cart.removeCart(id);
				Web.flash(req, "장바구니에서 뺐어요");
				break;
			case "/clear":
				cart.deleteBook();
				Web.flash(req, "장바구니를 비웠어요");
				break;
			default:
				resp.sendError(404);
				return;
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.redirect(req, resp, "/cart");
	}
}
