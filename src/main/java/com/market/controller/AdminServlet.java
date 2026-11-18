package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.BookDAO;
import com.market.dao.CouponDAO;
import com.market.dto.Book;
import com.market.dto.Coupon;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 관리자 — 원본 AdminPage(도서 추가)를 등록·수정·삭제와 쿠폰 관리로 넓혔다.
 * /admin(매출 요약), /admin/orders(주문 관리), /admin/books, /admin/books/edit(?id=), /admin/books/delete, /admin/coupons
 */
@WebServlet({ "/admin", "/admin/orders", "/admin/books", "/admin/books/edit", "/admin/books/delete", "/admin/coupons" })
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final BookDAO bookDAO = new BookDAO();
	private final CouponDAO couponDAO = new CouponDAO();
	private final com.market.dao.OrderDAO orderDAO = new com.market.dao.OrderDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			switch (req.getServletPath()) {
			case "/admin":
				req.setAttribute("summary", orderDAO.summary());
				java.util.List<com.market.dao.OrderDAO.DayRevenue> days = orderDAO.dailyRevenue(7);
				long max = days.stream().mapToLong(com.market.dao.OrderDAO.DayRevenue::amount).max().orElse(0);
				req.setAttribute("days", days);
				req.setAttribute("dayMax", Math.max(max, 1));
				req.setAttribute("weekTotal", days.stream().mapToLong(com.market.dao.OrderDAO.DayRevenue::amount).sum());
				req.setAttribute("recentOrders", orderDAO.findAll(null).stream().limit(5).toList());
				Web.render(req, resp, "admin/dashboard");
				break;
			case "/admin/orders":
				String status = req.getParameter("status");
				if (!"ORDERED".equals(status) && !"CANCELLED".equals(status))
					status = "";
				req.setAttribute("status", status);
				req.setAttribute("orders", orderDAO.findAll(status));
				Web.render(req, resp, "admin/orders");
				break;
			case "/admin/books":
				req.setAttribute("books", bookDAO.findAll());
				Web.render(req, resp, "admin/books");
				break;
			case "/admin/books/edit":
				String id = req.getParameter("id");
				if (id != null) {
					Book b = bookDAO.findById(id);
					if (b == null) {
						resp.sendError(404);
						return;
					}
					req.setAttribute("book", b);
					req.setAttribute("editing", true);
				} else {
					req.setAttribute("book", new Book());
				}
				Web.render(req, resp, "admin/book-form");
				break;
			case "/admin/coupons":
				req.setAttribute("coupons", couponDAO.findAll());
				Web.render(req, resp, "admin/coupons");
				break;
			default:
				resp.sendError(405);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			switch (req.getServletPath()) {
			case "/admin/books/edit":
				saveBook(req, resp);
				break;
			case "/admin/books/delete":
				if (bookDAO.delete(req.getParameter("id")))
					Web.flash(req, "도서를 지웠어요");
				else
					Web.flash(req, "주문 기록이 있는 도서라 지울 수 없어요");
				Web.redirect(req, resp, "/admin/books");
				break;
			case "/admin/coupons":
				saveCoupon(req, resp);
				break;
			default:
				resp.sendError(405);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private void saveBook(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		boolean editing = "true".equals(req.getParameter("editing"));
		Book b = new Book(Web.trim(req.getParameter("bookId")).toUpperCase(), Web.trim(req.getParameter("name")),
				Web.toInt(req.getParameter("unitPrice"), -1), Web.trim(req.getParameter("author")),
				Web.trim(req.getParameter("description")), Web.trim(req.getParameter("category")),
				Web.trim(req.getParameter("releaseDate")).replace('-', '/'), null);
		b.setPublisher(Web.trim(req.getParameter("publisher")));
		String image = Web.trim(req.getParameter("image"));
		String error = null;
		if (!editing && !b.getBookId().matches("ISBN[0-9]{4,13}|[0-9]{10}|[0-9]{13}"))
			error = "도서 ID는 ISBN 13자리 숫자나 ISBN 뒤에 숫자를 붙여 주세요. 예: 9788968481475, ISBN1237";
		else if (b.getName().isEmpty())
			error = "도서명을 입력해 주세요.";
		else if (!image.isEmpty() && !image.matches("https?://\\S+|[A-Za-z0-9_.-]+\\.(jpg|jpeg|png|gif|webp)"))
			error = "표지는 http 주소나 images/books 안의 파일 이름으로 넣어 주세요.";
		else if (b.getUnitPrice() < 0)
			error = "가격은 0 이상의 숫자로 입력해 주세요.";
		else if (!editing && bookDAO.findById(b.getBookId()) != null)
			error = "이미 있는 도서 ID예요. 다른 번호를 써 주세요.";

		if (error != null) {
			req.setAttribute("error", error);
			req.setAttribute("book", b);
			req.setAttribute("editing", editing);
			Web.render(req, resp, "admin/book-form");
			return;
		}
		b.setImage(image.isEmpty() ? null : image);
		if (editing) {
			bookDAO.update(b);
			Web.flash(req, "도서 정보를 고쳤어요");
		} else {
			bookDAO.insert(b);
			Web.flash(req, "새 도서를 등록했어요");
		}
		Web.redirect(req, resp, "/admin/books");
	}

	private void saveCoupon(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		if ("toggle".equals(req.getParameter("action"))) {
			couponDAO.toggleActive(req.getParameter("code"));
			Web.flash(req, "쿠폰 상태를 바꿨어요");
			Web.redirect(req, resp, "/admin/coupons");
			return;
		}
		Coupon c = new Coupon();
		c.setCode(Web.trim(req.getParameter("code")).toUpperCase());
		c.setName(Web.trim(req.getParameter("name")));
		c.setType("AMOUNT".equals(req.getParameter("type")) ? "AMOUNT" : "RATE");
		c.setValue(Web.toInt(req.getParameter("value"), 0));
		c.setMinAmount(Math.max(0, Web.toInt(req.getParameter("minAmount"), 0)));
		int max = Web.toInt(req.getParameter("maxDiscount"), 0);
		c.setMaxDiscount(max > 0 ? max : null);

		String error = null;
		if (!c.getCode().matches("[A-Z0-9]{4,20}"))
			error = "쿠폰 번호는 영문 대문자와 숫자로 4~20자를 써 주세요.";
		else if (c.getName().isEmpty())
			error = "쿠폰 이름을 입력해 주세요.";
		else if (c.getValue() <= 0 || ("RATE".equals(c.getType()) && c.getValue() > 90))
			error = "할인 값을 확인해 주세요. 정률은 1~90%, 정액은 1원 이상이에요.";
		else if (couponDAO.findByCode(c.getCode()) != null)
			error = "이미 있는 쿠폰 번호예요.";
		if (error != null) {
			req.setAttribute("error", error);
			req.setAttribute("form", c);
			req.setAttribute("coupons", couponDAO.findAll());
			Web.render(req, resp, "admin/coupons");
			return;
		}
		couponDAO.insert(c);
		Web.flash(req, "쿠폰을 만들었어요");
		Web.redirect(req, resp, "/admin/coupons");
	}
}
