package com.market.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 관리자가 회원의 상태·권한을 바꾸면, 이미 로그인해 있는 그 회원의 세션도 다음 요청부터 반영되게 한다.
 * 바뀐 회원 번호마다 "바뀐 순번"을 적어 두고, SecurityFilter가 세션의 "로그인(또는 마지막 반영) 순번"과 비교한다.
 * 톰캣 한 대에서 도는 앱이라 메모리로 충분하다 (서버를 다시 켜면 세션도 모두 사라진다).
 */
public final class MemberSessions {
	private static final AtomicLong SEQ = new AtomicLong();
	private static final Map<Long, Long> CHANGED = new ConcurrentHashMap<>();

	private MemberSessions() {
	}

	/** 지금 순번 (로그인할 때 세션에 적어 둔다) */
	public static long stamp() {
		return SEQ.incrementAndGet();
	}

	/** 관리자가 이 회원을 바꿨다고 표시 */
	public static void changed(long memberNo) {
		CHANGED.put(memberNo, SEQ.incrementAndGet());
	}

	/** 세션에 적힌 순번 뒤에 바뀐 적이 있으면 true */
	public static boolean isStale(long memberNo, long sessionStamp) {
		Long c = CHANGED.get(memberNo);
		return c != null && c > sessionStamp;
	}
}
