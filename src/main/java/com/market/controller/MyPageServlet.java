package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.market.dao.AddressDAO;
import com.market.dao.MemberDAO;
import com.market.dto.Address;
import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 내 정보 — 원본 bt1 "고객 정보 확인하기"
 * GET  /mypage                       내 정보 + 배송지 목록
 * POST /mypage                       이름·연락처 저장
 * POST /mypage/password              비밀번호 변경 (자체 가입 회원)
 * POST /mypage/address               배송지 추가 (최대 5개)
 * POST /mypage/address/default       기본 배송지로
 * POST /mypage/address/delete        배송지 지우기
 * POST /mypage/withdraw              회원 탈퇴 (주문 기록은 남기고 개인정보만 지움)
 */
@WebServlet({ "/mypage", "/mypage/*" })
public class MyPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PHONE = "[0-9\\-]{9,13}";
	private final MemberDAO memberDAO = new MemberDAO();
	private final AddressDAO addressDAO = new AddressDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getPathInfo() != null) {
			Web.redirect(req, resp, "/mypage");
			return;
		}
		render(req, resp);
	}

	private void render(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			req.setAttribute("addresses", addressDAO.findByMember(Web.member(req).getMemberNo()));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("maxAddresses", AddressDAO.MAX);
		Web.render(req, resp, "mypage");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		String path = req.getPathInfo() == null ? "" : req.getPathInfo();
		try {
			switch (path) {
			case "" -> saveProfile(req, resp, m);
			case "/password" -> changePassword(req, resp, m);
			case "/address" -> addAddress(req, resp, m);
			case "/address/default" -> {
				addressDAO.setDefault(m.getMemberNo(), Web.toInt(req.getParameter("id"), -1));
				refreshMember(req, m);
				Web.flash(req, "기본 배송지를 바꿨어요");
				Web.redirect(req, resp, "/mypage#addresses");
			}
			case "/address/delete" -> {
				addressDAO.delete(m.getMemberNo(), Web.toInt(req.getParameter("id"), -1));
				refreshMember(req, m);
				Web.flash(req, "배송지를 지웠어요");
				Web.redirect(req, resp, "/mypage#addresses");
			}
			case "/withdraw" -> withdraw(req, resp, m);
			default -> resp.sendError(404);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private void saveProfile(HttpServletRequest req, HttpServletResponse resp, Member m)
			throws SQLException, ServletException, IOException {
		String name = Web.trim(req.getParameter("name"));
		String phone = Web.trim(req.getParameter("phone"));
		String address = Web.trim(req.getParameter("address"));
		if (name.isEmpty() || (!phone.isEmpty() && !phone.matches(PHONE))) {
			req.setAttribute("error", "이름을 입력하고, 연락처는 숫자와 - 로 입력해 주세요.");
			render(req, resp);
			return;
		}
		m.setName(name);
		m.setPhone(phone.isEmpty() ? null : phone);
		m.setAddress(address.isEmpty() ? null : address);
		memberDAO.updateProfile(m);
		Web.flash(req, "내 정보를 저장했어요");
		Web.redirect(req, resp, "/mypage");
	}

	private void changePassword(HttpServletRequest req, HttpServletResponse resp, Member m)
			throws SQLException, ServletException, IOException {
		String current = req.getParameter("currentPassword");
		String next = req.getParameter("newPassword");
		String next2 = req.getParameter("newPassword2");
		String error = null;
		if (m.isSocial())
			error = "소셜 로그인 회원은 비밀번호가 없어요.";
		else if (next == null || next.length() < 8 || !next.matches(".*[A-Za-z].*") || !next.matches(".*[0-9].*"))
			error = "새 비밀번호는 영문과 숫자를 섞어 8자 이상으로 정해 주세요.";
		else if (!next.equals(next2))
			error = "새 비밀번호 두 칸이 서로 달라요.";
		else if (next.equals(current))
			error = "지금 비밀번호와 다른 비밀번호로 정해 주세요.";
		else if (!memberDAO.changePassword(m.getMemberNo(), current == null ? "" : current, next))
			error = "지금 비밀번호가 맞지 않아요.";
		if (error != null) {
			req.setAttribute("pwError", error);
			render(req, resp);
			return;
		}
		Web.flash(req, "비밀번호를 바꿨어요");
		Web.redirect(req, resp, "/mypage");
	}

	private void addAddress(HttpServletRequest req, HttpServletResponse resp, Member m)
			throws SQLException, ServletException, IOException {
		Address a = new Address();
		a.setMemberNo(m.getMemberNo());
		a.setLabel(Web.trim(req.getParameter("label")));
		a.setReceiverName(Web.trim(req.getParameter("receiverName")));
		a.setPhone(Web.trim(req.getParameter("phone")));
		a.setAddress(Web.trim(req.getParameter("address")));
		String error = null;
		if (a.getLabel().isEmpty() || a.getLabel().length() > 20)
			error = "배송지 이름(예: 집, 회사)을 20자 안으로 입력해 주세요.";
		else if (a.getReceiverName().isEmpty())
			error = "받는 분 이름을 입력해 주세요.";
		else if (!a.getPhone().matches(PHONE))
			error = "연락처는 숫자와 - 로 입력해 주세요. 예: 010-1234-5678";
		else if (a.getAddress().length() < 5)
			error = "주소를 동·호수까지 입력해 주세요.";
		else if (!addressDAO.add(a, req.getParameter("makeDefault") != null))
			error = "배송지는 " + AddressDAO.MAX + "개까지 저장할 수 있어요. 안 쓰는 배송지를 먼저 지워 주세요.";
		if (error != null) {
			req.setAttribute("addrError", error);
			req.setAttribute("addrForm", a);
			render(req, resp);
			return;
		}
		refreshMember(req, m);
		Web.flash(req, "배송지를 저장했어요");
		Web.redirect(req, resp, "/mypage#addresses");
	}

	private void withdraw(HttpServletRequest req, HttpServletResponse resp, Member m)
			throws SQLException, ServletException, IOException {
		String error = null;
		if (m.isAdmin())
			error = "관리자 계정은 탈퇴할 수 없어요. 다른 관리자가 권한을 바꾼 뒤 탈퇴해 주세요.";
		else if (memberDAO.hasOpenOrders(m.getMemberNo()))
			error = "배송이 끝나지 않은 주문이 있어요. 배송이 끝나거나 주문을 취소한 뒤 탈퇴할 수 있어요.";
		else if (m.isSocial() ? !"탈퇴".equals(Web.trim(req.getParameter("confirm")))
				: !memberDAO.checkPassword(m.getMemberNo(), req.getParameter("password") == null ? "" : req.getParameter("password")))
			error = m.isSocial() ? "확인 칸에 ‘탈퇴’를 입력해 주세요." : "비밀번호가 맞지 않아요.";
		if (error != null) {
			req.setAttribute("leaveError", error);
			render(req, resp);
			return;
		}
		memberDAO.withdraw(m.getMemberNo());
		com.market.util.MemberSessions.changed(m.getMemberNo()); // 다른 PC에 로그인해 있던 세션도 끊는다
		req.getSession().invalidate();
		Web.flash(req, "탈퇴했어요. 그동안 이용해 주셔서 고마워요");
		Web.redirect(req, resp, "/books");
	}

	/** 기본 배송지가 바뀌면 세션의 회원 정보(주문서 자동 채우기)도 맞춘다 */
	private void refreshMember(HttpServletRequest req, Member m) throws SQLException {
		Member fresh = memberDAO.findByNo(m.getMemberNo());
		if (fresh != null) {
			m.setAddress(fresh.getAddress());
			m.setPhone(fresh.getPhone());
		}
	}
}
