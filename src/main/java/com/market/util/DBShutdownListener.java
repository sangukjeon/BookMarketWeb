package com.market.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * 톰캣이 웹앱을 멈추거나 다시 올릴 때 DB 연결 풀을 닫는다.
 * 닫지 않으면 이클립스에서 저장할 때마다(자동 재시작) 연결이 쌓이고
 * 톰캣이 "memory leak" 경고를 띄운다. web.xml에 등록한다.
 */
public class DBShutdownListener implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			DBUtil.shutdown();
		} catch (LinkageError e) {
			// db.properties가 없어서 DBUtil이 처음부터 못 만들어진 경우: 닫을 풀이 없다
		}
	}
}
