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
 *  GET  /oauth/welcome                  소셜로 처음 가입한 회원의 추가 정보 입력 (연락처·주소)
 *  POST /oauth/welcome                  추가 정보 저장
 *  GET  /oauth/mock                     (목업 전용) 제공자 로그인 화면 흉내
 *  POST /oauth/mock                     (목업 전용) code를 발급하고 callback으로 돌려보낸다
 */
@WebServlet("/oauth/*")
public class SocialLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final SecureRandom RANDOM = new SecureRandom();
	private final MemberDAO memberDAO = new MemberDAO();

	private String callbackUrl(HttpServletRequest req, SocialProvider p) {
		// 개발자 콘솔에 등록한 Redirect URI와 글자 하나까지 같아야 해서, 설정값이 있으면 그것을 쓴다
		String base = SocialProviders.redirectBase();
		if (base.isEmpty())
			base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
		else if (base.endsWith("/"))
			base = base.substring(0, base.length() - 1);
		return base + "/oauth/" + p.id().toLowerCase() + "/callback";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String[] parts = req.getPathInfo() == null ? new String[0] : req.getPathInfo().split("/");
		HttpSession session = req.getSession();

		if (parts.length == 2 && parts[1].equals("welcome")) {
			Member me = Web.member(req);
			if (me == null) {
				Web.redirect(req, resp, "/login");
				return;
			}
			req.setAttribute("me", me);
			req.setAttribute("next", Web.safeNext((String) session.getAttribute("oauthNext")));
			Web.render(req, resp, "oauth-welcome");
			return;
		}
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
			SocialProfile profile = p.fetchProfile(req.getParameter("code"), req.getParameter("state"), callbackUrl(req, p));
			if (profile == null) {
				Web.flash(req, p.label() + " 정보를 받아 오지 못했어요. 다시 시도해 주세요.");
				Web.redirect(req, resp, "/login");
				return;
			}
			try {
				boolean isNew = memberDAO.findSocial(profile.provider(), profile.providerId()) == null;
				Member m = memberDAO.findOrCreateSocial(profile.provider(), profile.providerId(), profile.name(),
						profile.email());
				if (m.isSuspended()) {
					Web.flash(req, "이용이 정지된 계정이에요. 관리자에게 문의해 주세요.");
					Web.redirect(req, resp, "/login");
					return;
				}
				Web.login(req, m);
				if (isNew) { // 처음 가입: 배송에 쓸 연락처·주소를 받는 화면으로
					req.getSession().setAttribute("oauthNext", next);
					Web.flash(req, p.label() + " 계정으로 가입했어요. 배송 정보를 입력해 주세요");
					Web.redirect(req, resp, "/oauth/welcome");
					return;
				}
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
		if ("/welcome".equals(req.getPathInfo())) {
			saveWelcome(req, resp);
			return;
		}
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

	/** 처음 가입한 소셜 회원의 이름·연락처·주소 저장 */
	private void saveWelcome(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member me = Web.member(req);
		if (me == null) {
			Web.redirect(req, resp, "/login");
			return;
		}
		String next = Web.safeNext((String) req.getSession().getAttribute("oauthNext"));
		String name = Web.trim(req.getParameter("name"));
		String phone = Web.trim(req.getParameter("phone"));
		String address = Web.trim(req.getParameter("address"));
		if (name.isEmpty() || (!phone.isEmpty() && !phone.matches("[0-9\\-]{9,13}"))) {
			me.setName(name);
			me.setPhone(phone);
			me.setAddress(address);
			req.setAttribute("me", me);
			req.setAttribute("next", next);
			req.setAttribute("error", name.isEmpty() ? "이름을 입력해 주세요." : "연락처는 숫자와 - 로 9~13자로 입력해 주세요.");
			Web.render(req, resp, "oauth-welcome");
			return;
		}
		me.setName(name);
		me.setPhone(phone.isEmpty() ? null : phone);
		me.setAddress(address.isEmpty() ? null : address);
		try {
			memberDAO.updateProfile(me);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.getSession().removeAttribute("oauthNext");
		Web.flash(req, name + "님, 환영해요");
		Web.redirect(req, resp, next);
	}
}
