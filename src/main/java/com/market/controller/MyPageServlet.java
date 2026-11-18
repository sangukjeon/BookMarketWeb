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

/** 내 정보 보기·고치기 — 원본 bt1 "고객 정보 확인하기" */
@WebServlet("/mypage")
public class MyPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final MemberDAO memberDAO = new MemberDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Web.render(req, resp, "mypage");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		String name = Web.trim(req.getParameter("name"));
		String phone = Web.trim(req.getParameter("phone"));
		String address = Web.trim(req.getParameter("address"));
		if (name.isEmpty() || (!phone.isEmpty() && !phone.matches("[0-9\\-]{9,13}"))) {
			req.setAttribute("error", "이름을 입력하고, 연락처는 숫자와 - 로 입력해 주세요.");
			Web.render(req, resp, "mypage");
			return;
		}
		m.setName(name);
		m.setPhone(phone.isEmpty() ? null : phone);
		m.setAddress(address.isEmpty() ? null : address);
		try {
			memberDAO.updateProfile(m);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Web.flash(req, "내 정보를 저장했어요");
		Web.redirect(req, resp, "/mypage");
	}
}
