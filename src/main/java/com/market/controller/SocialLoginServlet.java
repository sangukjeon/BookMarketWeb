package com.market.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

import com.market.dao.MemberDAO;
import com.market.dto.Member;
import com.market.oauth.MockSocialProvider;
import com.market.oauth.SocialProfile;
import com.market.oauth.SocialProvider;
import com.market.oauth.SocialProviders;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 소셜 로그인 (OAuth 2.0 인가 코드 방식)
 *  GET  /oauth/{google|naver|kakao}/login     state를 만들고 제공자 로그인 화면으로 보낸다
 *  GET  /oauth/{google|naver|kakao}/callback  state 확인 → code로 사용자 정보 → 가입 또는 로그인
 *  GET  /oauth/mock                     (목업 전용) 제공자 로그인 화면 흉내
 *  POST /oauth/mock                     (목업 전용) code를 발급하고 callback으로 돌려보낸다
 */
@WebServlet("/oauth/*")
public class SocialLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final SecureRandom RANDOM = new SecureRandom();
	private final MemberDAO memberDAO = new MemberDAO();

	private String callbackUrl(HttpServletRequest req, SocialProvider p) {
		String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
		return base + "/oauth/" + p.id().toLowerCase() + "/callback";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String[] parts = req.getPathInfo() == null ? new String[0] : req.getPathInfo().split("/");
		HttpSession session = req.getSession();

		if (parts.length == 2 && parts[1].equals("mock")) {
			SocialProvider p = SocialProviders.get(req.getParameter("provider"));
			if (!(p instanceof MockSocialProvider)) {
				resp.sendError(404);
				return;
			}
			req.setAttribute("provider", p);
			req.setAttribute("state", req.getParameter("state"));
			Web.render(req, resp, "oauth-mock");
			return;
		}
		if (parts.length != 3) {
			resp.sendError(404);
			return;
		}
		SocialProvider p = SocialProviders.get(parts[1]);
		if (p == null) {
			resp.sendError(404);
			return;
		}

		if (parts[2].equals("login")) {
			byte[] b = new byte[16];
			RANDOM.nextBytes(b);
			String state = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
			session.setAttribute("oauthState", state);
			session.setAttribute("oauthNext", Web.safeNext(req.getParameter("next")));
			resp.sendRedirect(p.authorizeUrl(req.getContextPath(), state, callbackUrl(req, p)));
			return;
		}

		if (parts[2].equals("callback")) {
			String expected = (String) session.getAttribute("oauthState");
			session.removeAttribute("oauthState");
			String next = Web.safeNext((String) session.getAttribute("oauthNext"));
			if (expected == null || !expected.equals(req.getParameter("state"))) {
				Web.flash(req, "로그인 요청을 확인하지 못했어요. 다시 시도해 주세요.");
				Web.redirect(req, resp, "/login");
				return;
			}
			if (req.getParameter("error") != null) {
				Web.flash(req, p.label() + " 로그인을 취소했어요");
				Web.redirect(req, resp, "/login");
				return;
			}
			SocialProfile profile = p.fetchProfile(req.getParameter("code"), callbackUrl(req, p));
			if (profile == null) {
				Web.flash(req, p.label() + " 정보를 받아 오지 못했어요. 다시 시도해 주세요.");
				Web.redirect(req, resp, "/login");
				return;
			}
			try {
				Member m = memberDAO.findOrCreateSocial(profile.provider(), profile.providerId(), profile.name(),
						profile.email());
				if (m.isSuspended()) {
					Web.flash(req, "이용이 정지된 계정이에요. 관리자에게 문의해 주세요.");
					Web.redirect(req, resp, "/login");
					return;
				}
				Web.login(req, m);
				Web.flash(req, p.label() + " 계정으로 로그인했어요");
				Web.redirect(req, resp, next);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
			return;
		}
		resp.sendError(404);
	}

	/** 목업 제공자 화면의 "계속하기" / "취소" */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!"/mock".equals(req.getPathInfo())) {
			resp.sendError(404);
			return;
		}
		SocialProvider p = SocialProviders.get(req.getParameter("provider"));
		if (!(p instanceof MockSocialProvider mock)) {
			resp.sendError(404);
			return;
		}
		String state = java.net.URLEncoder.encode(Web.trim(req.getParameter("state")), "UTF-8");
		String callback = callbackUrl(req, p);
		if ("cancel".equals(req.getParameter("action"))) {
			resp.sendRedirect(callback + "?error=access_denied&state=" + state);
			return;
		}
		String name = Web.trim(req.getParameter("name"));
		String email = Web.trim(req.getParameter("email"));
		if (name.isEmpty() || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
			req.setAttribute("provider", p);
			req.setAttribute("state", req.getParameter("state"));
			req.setAttribute("name", name);
			req.setAttribute("email", email);
			req.setAttribute("error", "이름과 이메일을 바르게 입력해 주세요.");
			Web.render(req, resp, "oauth-mock");
			return;
		}
		String code = mock.issueCode(name, email);
		resp.sendRedirect(callback + "?code=" + code + "&state=" + state);
	}
}
