package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.market.cart.Cart;
import com.market.cart.CartItem;
import com.market.dao.CouponDAO;
import com.market.dao.MemberDAO;
import com.market.dao.OrderDAO;
import com.market.dto.Coupon;
import com.market.dto.Member;
import com.market.dto.Order;
import com.market.dto.OrderItem;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 주문서 — 원본 CartShippingPage(배송 정보) + 새로 만든 쿠폰 적용
 * GET  /order               주문서 화면
 * POST /order action=apply  쿠폰 적용 (입력한 배송 정보는 유지)
 * POST /order action=place  주문 저장 → 영수증으로 이동
 */
@WebServlet("/order")
public class OrderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final CouponDAO couponDAO = new CouponDAO();
	private final OrderDAO orderDAO = new OrderDAO();
	private final MemberDAO memberDAO = new MemberDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (Web.cart(req).isEmpty()) {
			Web.flash(req, "장바구니가 비어 있어요. 먼저 책을 담아 주세요");
			Web.redirect(req, resp, "/books");
			return;
		}
		Member m = Web.member(req);
		req.setAttribute("receiverName", m.getName());
		req.setAttribute("receiverPhone", m.getPhone());
		req.setAttribute("address", m.getAddress());
		show(req, resp, null);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Cart cart = Web.cart(req);
		if (cart.isEmpty()) {
			Web.redirect(req, resp, "/books");
			return;
		}
		Member m = Web.member(req);
		String name = Web.trim(req.getParameter("receiverName"));
		String phone = Web.trim(req.getParameter("receiverPhone"));
		String address = Web.trim(req.getParameter("address"));
		String couponCode = Web.trim(req.getParameter("couponCode")).toUpperCase();
		boolean picked = req.getParameter("couponPick") != null; // 쿠폰 칩을 누른 경우
		if (picked)
			couponCode = Web.trim(req.getParameter("couponPick")).toUpperCase();
		req.setAttribute("receiverName", name);
		req.setAttribute("receiverPhone", phone);
		req.setAttribute("address", address);
		req.setAttribute("saveAddress", req.getParameter("saveAddress") != null);

		try {
			Coupon coupon = null;
			String couponError = null;
			if (!couponCode.isEmpty()) {
				coupon = couponDAO.findByCode(couponCode);
				if (coupon == null || !coupon.isActive()) {
					couponError = "쓸 수 없는 쿠폰 번호예요. 번호를 다시 확인해 주세요.";
					coupon = null;
				} else if (orderDAO.hasUsedCoupon(m.getMemberNo(), coupon.getCode())) {
					couponError = "이미 쓴 쿠폰이에요. 이 쿠폰으로 한 주문을 취소하면 다시 쓸 수 있어요.";
					coupon = null;
				} else if (coupon.discountFor(cart.getItemTotal()) == 0) {
					couponError = String.format("%,d원 이상 담으면 쓸 수 있는 쿠폰이에요.", coupon.getMinAmount());
					coupon = null;
				}
			}
			req.setAttribute("couponCode", couponCode);
			req.setAttribute("couponError", couponError);

			if (picked || !"place".equals(req.getParameter("action"))) {
				show(req, resp, coupon);
				return;
			}

			String error = null;
			if (name.isEmpty())
				error = "받는 분 이름을 입력해 주세요.";
			else if (!phone.matches("[0-9\\-]{9,13}"))
				error = "연락처는 숫자와 - 로 입력해 주세요. 예: 010-1234-5678";
			else if (address.length() < 5)
				error = "배송지를 동·호수까지 입력해 주세요.";
			else if (couponError != null)
				error = "쿠폰을 확인해 주세요. 쓰지 않으려면 쿠폰 칸을 비우면 돼요.";
			if (error != null) {
				req.setAttribute("error", error);
				show(req, resp, coupon);
				return;
			}

			Order o = buildOrder(cart, coupon);
			o.setMemberNo(m.getMemberNo());
			o.setReceiverName(name);
			o.setReceiverPhone(phone);
			o.setAddress(address);
			long orderId = orderDAO.insert(o);

			if (req.getParameter("saveAddress") != null) {
				m.setAddress(address);
				if (m.getPhone() == null || m.getPhone().isEmpty())
					m.setPhone(phone);
				memberDAO.updateProfile(m);
			}
			cart.deleteBook();
			Web.flash(req, "주문을 마쳤어요");
			Web.redirect(req, resp, "/orders/receipt?id=" + orderId);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	/** 장바구니 + 쿠폰으로 금액을 계산한다 (화면 미리보기와 저장에 똑같이 쓴다) */
	static Order buildOrder(Cart cart, Coupon coupon) {
		Order o = new Order();
		for (CartItem ci : cart.getItems()) {
			OrderItem it = new OrderItem();
			it.setBookId(ci.getBookID());
			it.setBookName(ci.getItemBook().getName());
			it.setUnitPrice(ci.getItemBook().getUnitPrice());
			it.setQuantity(ci.getQuantity());
			it.setLineTotal(ci.getTotalPrice());
			o.getItems().add(it);
		}
		int total = cart.getItemTotal();
		int discount = coupon == null ? 0 : coupon.discountFor(total);
		int shipping = cart.shippingFeeFor(total - discount);
		o.setItemTotal(total);
		o.setDiscount(discount);
		o.setShippingFee(shipping);
		o.setPayAmount(total - discount + shipping);
		o.setCouponCode(coupon == null ? null : coupon.getCode());
		return o;
	}

	private void show(HttpServletRequest req, HttpServletResponse resp, Coupon coupon)
			throws ServletException, IOException {
		try {
			List<Coupon> coupons = couponDAO.findActive();
			long memberNo = Web.member(req).getMemberNo();
			java.util.Set<String> used = new java.util.HashSet<>();
			for (Coupon c : coupons)
				if (orderDAO.hasUsedCoupon(memberNo, c.getCode()))
					used.add(c.getCode());
			req.setAttribute("coupons", coupons);
			req.setAttribute("usedCoupons", used);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("coupon", coupon);
		req.setAttribute("summary", buildOrder(Web.cart(req), coupon));
		req.setAttribute("freeShippingMin", Cart.FREE_SHIPPING_MIN);
		Web.render(req, resp, "order");
	}
}
