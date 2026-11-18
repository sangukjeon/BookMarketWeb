package com.market.dao;

import java.sql.SQLException;

/**
 * DB 종류와 상관없이 오류를 구분한다.
 * MySQL 드라이버는 SQLIntegrityConstraintViolationException을 던지지만 PostgreSQL 드라이버는 그렇지 않아서,
 * 표준 SQLSTATE 코드(23xxx = 무결성 제약 위반: 중복 키, 외래키 등)로 판단한다.
 */
final class DBErrors {
	private DBErrors() {
	}

	static boolean isConstraintViolation(SQLException e) {
		for (Throwable t = e; t != null; t = t.getCause())
			if (t instanceof SQLException s && s.getSQLState() != null && s.getSQLState().startsWith("23"))
				return true;
		return false;
	}
}
