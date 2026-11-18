package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.MemberDAO;
import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** 로그인·회원가입·로그아웃 — 원본 GuestWindow, AdminLoginDialog */
@WebServlet({ "/login", "/signup", "/logout" })
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final MemberDAO memberDAO = new MemberDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		if (path.equals("/logout")) {
			resp.sendError(405);
			return;
		}
		if (Web.member(req) != null) {
			Web.redirect(req, resp, "/books");
			return;
		}
		req.setAttribute("next", Web.safeNext(req.getParameter("next")));
		Web.render(req, resp, path.equals("/login") ? "login" : "signup");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		String next = Web.safeNext(req.getParameter("next"));
		try {
			if (path.equals("/logout")) {
				HttpSession s = req.getSession(false);
				if (s != null)
					s.invalidate();
				Web.flash(req, "로그아웃했어요");
				Web.redirect(req, resp, "/books");
				return;
			}
			String loginId = Web.trim(req.getParameter("loginId"));
			String password = req.getParameter("password") == null ? "" : req.getParameter("password");
			req.setAttribute("next", next);
			req.setAttribute("loginId", loginId);

			if (path.equals("/login")) {
				Member m = memberDAO.login(loginId, password);
				if (m == null) {
					req.setAttribute("error", "아이디 또는 비밀번호가 맞지 않아요. 다시 확인해 주세요.");
					Web.render(req, resp, "login");
					return;
				}
				if (m.isSuspended()) {
					req.setAttribute("error", "이용이 정지된 계정이에요. 관리자에게 문의해 주세요.");
					Web.render(req, resp, "login");
					return;
				}
				Web.login(req, m);
				Web.flash(req, m.getName() + "님, 반가워요");
				Web.redirect(req, resp, m.isAdmin() && next.equals("/books") ? "/admin" : next);
				return;
			}

			// 회원가입
			String name = Web.trim(req.getParameter("name"));
			String phone = Web.trim(req.getParameter("phone"));
			String address = Web.trim(req.getParameter("address"));
			req.setAttribute("name", name);
			req.setAttribute("phone", phone);
			req.setAttribute("address", address);

			String error = null;
			if (!loginId.matches("[a-zA-Z0-9]{4,20}"))
				error = "아이디는 영문과 숫자로 4~20자를 써 주세요.";
			else if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*"))
				error = "비밀번호는 영문과 숫자를 섞어 8자 이상으로 만들어 주세요.";
			else if (!password.equals(req.getParameter("password2")))
				error = "비밀번호 확인이 맞지 않아요. 같은 비밀번호를 한 번 더 입력해 주세요.";
			else if (name.isEmpty())
				error = "이름을 입력해 주세요.";
			else if (!phone.matches("[0-9\\-]{9,13}"))
				error = "연락처는 숫자와 - 로 입력해 주세요. 예: 010-1234-5678";
			else if (memberDAO.findByLoginId(loginId) != null)
				error = "이미 쓰고 있는 아이디예요. 다른 아이디를 골라 주세요.";

			if (error != null) {
				req.setAttribute("error", error);
				Web.render(req, resp, "signup");
				return;
			}
			Member m = memberDAO.signup(loginId, password, name, phone, address.isEmpty() ? null : address);
			Web.login(req, m);
			Web.flash(req, "가입을 마쳤어요. 바로 쇼핑할 수 있어요");
			Web.redirect(req, resp, next);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}
