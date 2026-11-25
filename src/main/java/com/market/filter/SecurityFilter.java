package com.market.filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import com.market.dao.MemberDAO;
import com.market.dto.Member;
import com.market.util.MemberSessions;
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
		if (m != null) {
			Object stamp = session.getAttribute(Web.LOGIN_STAMP);
			if (MemberSessions.isStale(m.getMemberNo(), stamp instanceof Long l ? l : 0L)) {
				m = refresh(req, resp, session, m);
				if (m == null)
					return;
			}
		}
		boolean needLogin = path.startsWith("/order") || path.startsWith("/mypage") || path.startsWith("/admin")
				|| path.startsWith("/wishlist") || path.startsWith("/books/review") || path.startsWith("/coupons")
				|| path.startsWith("/notifications") || path.startsWith("/restock") || path.startsWith("/attendance");
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

	/**
	 * 관리자가 이 회원의 상태·권한을 바꾼 뒤 처음 들어온 요청: DB에서 다시 읽는다.
	 * 정지됐으면 로그아웃시키고 로그인 화면으로 보낸 뒤 null을 돌려준다.
	 */
	private Member refresh(HttpServletRequest req, HttpServletResponse resp, HttpSession session, Member old)
			throws IOException, ServletException {
		Member fresh;
		try {
			fresh = new MemberDAO().findByNo(old.getMemberNo());
		} catch (java.sql.SQLException e) {
			throw new ServletException(e);
		}
		if (fresh == null || fresh.isSuspended() || fresh.isWithdrawn()) {
			session.invalidate();
			Web.flash(req, "이용이 정지된 계정이라 로그아웃됐어요. 관리자에게 문의해 주세요.");
			resp.sendRedirect(req.getContextPath() + "/login");
			return null;
		}
		fresh.setPasswordHash(null);
		fresh.setSalt(null);
		session.setAttribute(Web.LOGIN, fresh);
		session.setAttribute(Web.LOGIN_STAMP, MemberSessions.stamp());
		return fresh;
	}
}
