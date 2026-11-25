package com.market.util;

import java.io.IOException;

import com.market.cart.Cart;
import com.market.dto.Member;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** 서블릿에서 자주 쓰는 일을 모은 도우미 */
public final class Web {
	public static final String LOGIN = "loginMember";
	public static final String LOGIN_STAMP = "loginStamp";
	public static final String CART = "cart";
	public static final String FLASH = "flash";

	private Web() {
	}

	public static void render(HttpServletRequest req, HttpServletResponse resp, String view)
			throws ServletException, IOException {
		if (view.equals("login") || view.equals("signup"))
			req.setAttribute("socialMock", com.market.oauth.SocialProviders.anyMock());
		req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
	}

	public static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
		resp.sendRedirect(req.getContextPath() + path);
	}

	public static Member member(HttpServletRequest req) {
		HttpSession s = req.getSession(false);
		return s == null ? null : (Member) s.getAttribute(LOGIN);
	}

	public static Cart cart(HttpServletRequest req) {
		HttpSession s = req.getSession();
		Cart cart = (Cart) s.getAttribute(CART);
		if (cart == null) {
			cart = new Cart();
			s.setAttribute(CART, cart);
		}
		return cart;
	}

	/** 다음 화면에서 한 번만 보이는 토스트 문구 */
	public static void flash(HttpServletRequest req, String message) {
		req.getSession().setAttribute(FLASH, message);
	}

	/** 로그인: 세션 고정 공격을 막기 위해 세션 ID를 새로 받는다. 장바구니는 유지 */
	public static void login(HttpServletRequest req, Member m) {
		req.changeSessionId();
		m.setPasswordHash(null);
		m.setSalt(null);
		req.getSession().setAttribute(LOGIN, m);
		req.getSession().setAttribute(LOGIN_STAMP, MemberSessions.stamp());
	}

	/** 로그인 뒤 돌아갈 주소. 다른 사이트로 보내지 않도록 우리 경로만 허용 */
	public static String safeNext(String next) {
		if (next == null || !next.startsWith("/") || next.startsWith("//") || next.contains("\\"))
			return "/books";
		return next;
	}

	public static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	public static int toInt(String s, int def) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return def;
		}
	}
}
