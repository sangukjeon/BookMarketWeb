package com.market.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.market.dao.AttendanceDAO;
import com.market.dao.CouponDAO;
import com.market.dao.NotificationDAO;
import com.market.dto.Member;
import com.market.util.Clock;
import com.market.util.Web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 출석 체크 (로그인 필요)
 * GET  /attendance   이번 달 달력 + 연속 출석일
 * POST /attendance   오늘 출석 → 7일 연속이면 ATTEND7 쿠폰 (회원당 1번)
 */
@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String REWARD = "ATTEND7";
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final CouponDAO couponDAO = new CouponDAO();

	/** 달력 한 칸 (day가 0이면 빈칸) */
	public record Cell(int day, boolean checked, boolean today) {
		public int getDay() { return day; }
		public boolean isChecked() { return checked; }
		public boolean isToday() { return today; }
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		LocalDate today = Clock.today();
		LocalDate first = today.withDayOfMonth(1);
		try {
			Set<LocalDate> days = attendanceDAO.days(m.getMemberNo(), first, first.plusMonths(1).minusDays(1));
			List<Cell> cells = new ArrayList<>();
			int blank = first.getDayOfWeek().getValue() % 7; // 일요일 시작
			for (int i = 0; i < blank; i++)
				cells.add(new Cell(0, false, false));
			for (int d = 1; d <= first.lengthOfMonth(); d++) {
				LocalDate x = first.withDayOfMonth(d);
				cells.add(new Cell(d, days.contains(x), x.equals(today)));
			}
			req.setAttribute("cells", cells);
			req.setAttribute("monthLabel", today.getYear() + "년 " + today.getMonthValue() + "월");
			req.setAttribute("checkedToday", days.contains(today));
			req.setAttribute("monthCount", days.size());
			int streak = attendanceDAO.streak(m.getMemberNo(), today);
			req.setAttribute("streak", streak);
			req.setAttribute("goal", AttendanceDAO.GOAL_DAYS);
			req.setAttribute("rewarded", couponDAO.inWallet(m.getMemberNo(), REWARD));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("nav", "attendance");
		Web.render(req, resp, "attendance");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Member m = Web.member(req);
		LocalDate today = Clock.today();
		try {
			if (!attendanceDAO.check(m.getMemberNo(), today)) {
				Web.flash(req, "오늘은 이미 출석했어요. 내일 또 만나요");
			} else {
				int streak = attendanceDAO.streak(m.getMemberNo(), today);
				if (streak >= AttendanceDAO.GOAL_DAYS && couponDAO.grantReward(m.getMemberNo(), REWARD)) {
					new NotificationDAO().add(m.getMemberNo(), "7일 연속 출석! 1,000원 쿠폰이 쿠폰함에 들어왔어요", "/coupons");
					Web.flash(req, "7일 연속 출석이에요. 1,000원 쿠폰을 쿠폰함에 넣었어요");
				} else
					Web.flash(req, "출석했어요. " + streak + "일째 이어서 오고 있어요");
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		String next = req.getParameter("next");
		Web.redirect(req, resp, next == null || next.isBlank() ? "/attendance" : Web.safeNext(next));
	}
}
