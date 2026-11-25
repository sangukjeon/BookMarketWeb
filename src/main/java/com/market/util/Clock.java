package com.market.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** 한국 시간 (DB 서버 시간대와 상관없이 Java에서 넣는다) */
public final class Clock {
	public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private Clock() {
	}

	public static LocalDateTime now() {
		return LocalDateTime.now(SEOUL).withNano(0);
	}

	public static LocalDate today() {
		return LocalDate.now(SEOUL);
	}

	public static Timestamp ts(LocalDateTime t) {
		return Timestamp.valueOf(t);
	}

	public static Timestamp nowTs() {
		return ts(now());
	}
}
