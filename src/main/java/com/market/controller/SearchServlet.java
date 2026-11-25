package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.market.dao.SearchDAO;
import com.market.dto.Member;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 검색창 도우미
 * GET  /search/suggest         {"recent":[…], "popular":[…]} (JSON) — 검색창을 누르면 ui.js가 부른다
 * POST /search/recent/delete   keyword(없으면 전체) — 내 최근 검색어 지우기
 * 회원은 search_log에서, 비회원은 세션에서 최근 검색어를 꺼낸다.
 */
@WebServlet({ "/search/suggest", "/search/recent/delete" })
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final String SESSION_RECENT = "recentSearches";
	private final SearchDAO searchDAO = new SearchDAO();

	/** BookServlet이 검색할 때 부른다 */
	@SuppressWarnings("unchecked")
	static void remember(HttpServletRequest req, String keyword, int resultCount) throws SQLException {
		String k = SearchDAO.normalize(keyword);
		if (k.length() < 2)
			return;
		Member m = Web.member(req);
		new SearchDAO().log(m == null ? null : m.getMemberNo(), k, resultCount);
		if (m == null) {
			HttpSession s = req.getSession();
			LinkedList<String> list = (LinkedList<String>) s.getAttribute(SESSION_RECENT);
			if (list == null)
				list = new LinkedList<>();
			list.remove(k);
			list.addFirst(k);
			while (list.size() > SearchDAO.RECENT_MAX)
				list.removeLast();
			s.setAttribute(SESSION_RECENT, list);
		}
	}

	@SuppressWarnings("unchecked")
	static List<String> recent(HttpServletRequest req) throws SQLException {
		Member m = Web.member(req);
		if (m != null)
			return new SearchDAO().recent(m.getMemberNo());
		HttpSession s = req.getSession(false);
		List<String> list = s == null ? null : (List<String>) s.getAttribute(SESSION_RECENT);
		return list == null ? List.of() : List.copyOf(list);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getServletPath().equals("/search/suggest")) {
			resp.sendError(405);
			return;
		}
		try {
			resp.setContentType("application/json; charset=UTF-8");
			resp.setHeader("Cache-Control", "no-store");
			resp.getWriter().write("{\"recent\":" + json(recent(req)) + ",\"popular\":" + json(searchDAO.popular()) + "}");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getServletPath().equals("/search/recent/delete")) {
			resp.sendError(405);
			return;
		}
		String keyword = req.getParameter("keyword");
		keyword = keyword == null || keyword.isBlank() ? null : SearchDAO.normalize(keyword);
		Member m = Web.member(req);
		try {
			if (m != null)
				searchDAO.hideRecent(m.getMemberNo(), keyword);
			else {
				HttpSession s = req.getSession(false);
				List<String> list = s == null ? null : (List<String>) s.getAttribute(SESSION_RECENT);
				if (list != null) {
					if (keyword == null)
						list.clear();
					else
						list.remove(keyword);
				}
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		if ("fetch".equals(req.getHeader("X-Requested-With"))) {
			resp.setStatus(204);
			return;
		}
		Web.redirect(req, resp, "/books");
	}

	static String json(List<String> list) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0)
				sb.append(',');
			sb.append('"');
			for (char c : list.get(i).toCharArray()) {
				switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '<' -> sb.append("\\u003c");
				case '>' -> sb.append("\\u003e");
				case '&' -> sb.append("\\u0026");
				default -> {
					if (c < 0x20)
						sb.append(String.format("\\u%04x", (int) c));
					else
						sb.append(c);
				}
				}
			}
			sb.append('"');
		}
		return sb.append(']').toString();
	}
}
