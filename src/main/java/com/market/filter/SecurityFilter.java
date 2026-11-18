package com.market.filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 1) CSRF 방지: 모든 POST 요청에 세션별 토큰(_csrf)이 있어야 한다.
 * 2) 접근 제어: 주문·주문내역·내정보는 로그인 필요, /admin 은 관리자만.
 */
public class SecurityFilter implements Filter {
	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;
		String path = req.getRequestURI().substring(req.getContextPath().length());

		// 정적 파일은 그냥 통과
		if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
			chain.doFilter(req, resp);
			return;
		}

		HttpSession session = req.getSession();
		if (session.getAttribute("csrf") == null) {
			byte[] b = new byte[24];
			RANDOM.nextBytes(b);
			session.setAttribute("csrf", Base64.getUrlEncoder().withoutPadding().encodeToString(b));
		}
		if ("POST".equals(req.getMethod()) && !session.getAttribute("csrf").equals(req.getParameter("_csrf"))) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "요청을 확인하지 못했어요. 화면을 새로 고친 뒤 다시 시도해 주세요.");
			return;
		}

		Member m = Web.member(req);
		boolean needLogin = path.startsWith("/order") || path.startsWith("/mypage") || path.startsWith("/admin")
				|| path.startsWith("/wishlist") || path.startsWith("/books/review");
		if (needLogin && m == null) {
			String next = req.getRequestURI().substring(req.getContextPath().length());
			if ("GET".equals(req.getMethod()) && req.getQueryString() != null)
				next += "?" + req.getQueryString();
			if (!"GET".equals(req.getMethod())) // 폼에 next가 있으면 로그인 뒤 그 화면으로
				next = req.getParameter("next") != null ? Web.safeNext(req.getParameter("next"))
						: path.startsWith("/order") ? "/cart" : "/books";
			Web.flash(req, "로그인이 필요해요. 로그인한 뒤 이어서 할 수 있어요.");
			resp.sendRedirect(req.getContextPath() + "/login?next=" + java.net.URLEncoder.encode(next, "UTF-8"));
			return;
		}
		if (path.startsWith("/admin") && !m.isAdmin()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 들어갈 수 있어요.");
			return;
		}
		chain.doFilter(req, resp);
	}
}
