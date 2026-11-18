package com.market.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

/**
 * JDBC 연결 도우미.
 * 접속 정보는 src/main/resources 가 아닌 WEB-INF/classes/db.properties
 * (이클립스에서는 src/main/java/db.properties)에서 읽는다.
 *
 * 연결 풀: 톰캣에 들어 있는 DBCP(tomcat-dbcp.jar)를 쓴다. 외부 jar를 따로 넣지 않아도 된다.
 * 클라우드 DB(TiDB)는 연결 한 번에 TLS 인증을 거치므로, 쿼리마다 새로 연결하면 화면이 느려진다.
 * 풀은 연결을 미리 열어 두고 돌려 쓰며, DAO의 con.close()는 연결을 끊지 않고 풀에 돌려준다.
 */
@SuppressWarnings("deprecation") // 톰캣 10.1 초기 버전(DBCP 2.9)에도 있는 밀리초 설정을 일부러 쓴다
public final class DBUtil {
	private static final long MINUTE = 60_000L;
	private static final BasicDataSource POOL = new BasicDataSource();
	/** db.properties 설정 실수(비밀번호를 안 넣음 등)를 연결 전에 알려 주기 위한 안내. 문제없으면 null */
	private static String configError;

	static {
		Properties p = new Properties();
		try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in == null)
				throw new IllegalStateException(
						"db.properties 파일을 찾지 못했어요. src/main/java/db.properties.example을 복사해서 db.properties로 만드세요.");
			// UTF-8로 읽는다 (기본 ISO-8859-1로 읽으면 한글이 깨져 PostgreSQL이 Prohibited character 오류를 낸다)
			p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
		// 드라이버 jar는 WEB-INF/lib에 있으므로 이 웹앱의 클래스 로더로 찾게 한다
		POOL.setDriverClassLoader(DBUtil.class.getClassLoader());
		POOL.setDriverClassName(p.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
		POOL.setUrl(p.getProperty("db.url"));
		POOL.setUsername(p.getProperty("db.user"));
		String pw = p.getProperty("db.password", "").trim();
		POOL.setPassword(pw);
		if (pw.isEmpty() || pw.startsWith("PUT_") || pw.startsWith("<") || pw.startsWith("여기에"))
			configError = "db.properties의 db.password에 실제 DB 비밀번호를 넣어 주세요. "
					+ "(Supabase: Project Settings → Database에서 확인·재설정) 넣은 뒤 Project Clean → 서버 Restart";
		POOL.setMaxTotal(intProp(p, "db.pool.maxTotal", 10));
		POOL.setMaxIdle(intProp(p, "db.pool.maxIdle", 5));
		POOL.setMinIdle(0);
		POOL.setMaxWaitMillis(intProp(p, "db.pool.maxWaitSeconds", 10) * 1000L);
		// 클라우드는 오래 쉬던 연결을 끊을 수 있다 → 꺼내기 전에 살아 있는지 확인
		POOL.setValidationQuery("SELECT 1");
		POOL.setTestOnBorrow(true);
		// 쉬는 연결은 5분마다 정리하고, 30분 넘게 쓴 연결은 새로 바꾼다
		POOL.setTimeBetweenEvictionRunsMillis(5 * MINUTE);
		POOL.setMinEvictableIdleTimeMillis(10 * MINUTE);
		POOL.setMaxConnLifetimeMillis(30 * MINUTE);
	}

	private DBUtil() {
	}

	private static int intProp(Properties p, String key, int def) {
		try {
			return Integer.parseInt(p.getProperty(key, "").trim());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/** 풀에서 연결을 하나 빌린다. try-with-resources로 닫으면 풀에 돌아간다 */
	public static Connection getConnection() throws SQLException {
		if (configError != null)
			throw new SQLException(configError);
		return POOL.getConnection();
	}

	/** 웹앱이 멈출 때(DBShutdownListener) 풀을 닫고, 이 웹앱이 올린 JDBC 드라이버를 내린다 */
	public static void shutdown() {
		try {
			POOL.close();
		} catch (SQLException e) {
			// 멈추는 중이라 더 할 일이 없다
		}
		ClassLoader mine = DBUtil.class.getClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver d = drivers.nextElement();
			if (d.getClass().getClassLoader() == mine) {
				try {
					DriverManager.deregisterDriver(d);
				} catch (SQLException e) {
					// 무시
				}
			}
		}
	}
}
