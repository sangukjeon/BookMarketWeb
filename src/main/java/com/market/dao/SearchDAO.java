package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.util.Clock;
import com.market.util.DBUtil;

/**
 * search_log — 최근 검색어(회원별)와 인기 검색어(모두).
 * 최근 검색어를 지우면 행은 남기고 member_no만 비운다 → 인기 순위는 그대로.
 */
public class SearchDAO {
	public static final int RECENT_MAX = 5;
	public static final int POPULAR_MAX = 10;
	private static final long CACHE_MS = 60_000;
	private static volatile List<String> popularCache;
	private static volatile long popularAt;

	/** 검색어 정리: 앞뒤 공백, 연속 공백 하나로, 100자까지 */
	public static String normalize(String q) {
		if (q == null)
			return "";
		String s = q.trim().replaceAll("\\s+", " ");
		return s.length() > 100 ? s.substring(0, 100) : s;
	}

	public void log(Long memberNo, String keyword, int resultCount) throws SQLException {
		String k = normalize(keyword);
		if (k.length() < 2)
			return;
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO search_log (member_no, keyword, result_count, searched_at) VALUES (?,?,?,?)")) {
			if (memberNo == null)
				ps.setNull(1, java.sql.Types.BIGINT);
			else
				ps.setLong(1, memberNo);
			ps.setString(2, k);
			ps.setInt(3, resultCount);
			ps.setTimestamp(4, Clock.nowTs());
			ps.executeUpdate();
		}
		if (resultCount > 0)
			clearCache(); // 새 검색이 순위에 바로 보이게
	}

	/** 내 최근 검색어 (같은 낱말은 한 번, 최근 순) */
	public List<String> recent(long memberNo) throws SQLException {
		List<String> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT keyword, MAX(log_id) AS last_id FROM search_log WHERE member_no = ? "
						+ "GROUP BY keyword ORDER BY last_id DESC LIMIT ?")) {
			ps.setLong(1, memberNo);
			ps.setInt(2, RECENT_MAX);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(rs.getString("keyword"));
			}
		}
		return list;
	}

	/** 최근 검색어 지우기 (keyword가 null이면 전체) */
	public void hideRecent(long memberNo, String keyword) throws SQLException {
		String sql = "UPDATE search_log SET member_no = NULL WHERE member_no = ?" + (keyword == null ? "" : " AND keyword = ?");
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, memberNo);
			if (keyword != null)
				ps.setString(2, normalize(keyword));
			ps.executeUpdate();
		}
	}

	/** 인기 검색어: 결과가 있었던 검색만, 최근 24시간 → 모자라면 7일. 1분 동안 기억해 둔다 */
	public List<String> popular() throws SQLException {
		List<String> cached = popularCache;
		if (cached != null && System.currentTimeMillis() - popularAt < CACHE_MS)
			return cached;
		List<String> list = popularSince(1);
		if (list.size() < 5)
			list = popularSince(7);
		popularCache = list;
		popularAt = System.currentTimeMillis();
		return list;
	}

	/** 테스트·관리용: 캐시 비우기 */
	public static void clearCache() {
		popularCache = null;
	}

	private List<String> popularSince(int days) throws SQLException {
		List<String> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT MIN(keyword) AS keyword, COUNT(*) AS cnt, MAX(searched_at) AS last_at FROM search_log "
						+ "WHERE searched_at >= ? AND result_count > 0 GROUP BY LOWER(keyword) "
						+ "ORDER BY cnt DESC, last_at DESC LIMIT ?")) {
			ps.setTimestamp(1, Clock.ts(Clock.now().minusDays(days)));
			ps.setInt(2, POPULAR_MAX);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(rs.getString("keyword"));
			}
		}
		return list;
	}
}
