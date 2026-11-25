package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.OrderDAO;
import com.market.dto.Member;
import com.market.dto.Order;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 주문 내역(/orders), 영수증(/orders/receipt) — 원본 CartOrderBillPage
 * POST /orders/cancel  주문 취소 (본인 주문, 관리자는 모든 주문)
 */
@WebServlet({ "/orders", "/orders/receipt", "/orders/cancel" })
public class OrdersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final OrderDAO orderDAO = new OrderDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		try {
			if (req.getServletPath().equals("/orders/receipt")) {
				long id = Web.toInt(req.getParameter("id"), -1);
				Order o = orderDAO.findReceipt(id, m.isAdmin() ? 0 : m.getMemberNo());
				if (o == null) {
					resp.sendError(404, "찾는 주문이 없어요.");
					return;
				}
				req.setAttribute("order", o);
				Web.render(req, resp, "receipt");
				return;
			}
			if (!req.getServletPath().equals("/orders")) {
				resp.sendError(405);
				return;
			}
			req.setAttribute("orders", orderDAO.findByMember(m.getMemberNo()));
			Web.render(req, resp, "orders");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getServletPath().equals("/orders/cancel")) {
			resp.sendError(405);
			return;
		}
		Member m = Web.member(req);
		long id = Web.toInt(req.getParameter("id"), -1);
		try {
			if (orderDAO.cancel(id, m.isAdmin() ? 0 : m.getMemberNo())) {
				Web.flash(req, "주문을 취소했어요. 쓴 쿠폰은 다시 쓸 수 있어요");
				// 취소로 재고가 돌아온 품절 책: 재입고 알림
				com.market.dto.Order o = orderDAO.findReceipt(id, 0);
				if (o != null)
					for (com.market.dto.OrderItem it : o.getItems())
						new com.market.dao.RestockDAO().notifyIfAvailable(it.getBookId());
			}
			else {
				com.market.dto.Order o = orderDAO.findReceipt(id, m.isAdmin() ? 0 : m.getMemberNo());
				if (o != null && ("SHIPPING".equals(o.getStatus()) || "DELIVERED".equals(o.getStatus())))
					Web.flash(req, "배송이 시작돼서 취소할 수 없어요");
				else
					Web.flash(req, "이미 취소됐거나 취소할 수 없는 주문이에요");
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		String next = req.getParameter("next");
		Web.redirect(req, resp, next == null ? "/orders/receipt?id=" + id : Web.safeNext(next));
	}
}
