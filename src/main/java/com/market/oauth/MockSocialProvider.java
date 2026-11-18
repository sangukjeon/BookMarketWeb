package com.market.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 목업 소셜 로그인. 실제 구글/네이버 서버 대신 우리 앱 안의 테스트 화면(/oauth/mock)이
 * "제공자" 역할을 한다. 흐름(state 검증 → code 발급 → code로 사용자 정보 교환)은 실제와 같다.
 */
public class MockSocialProvider implements SocialProvider {
	/** 목업 "제공자 서버"가 발급한 일회용 code → 사용자 정보 */
	private static final Map<String, SocialProfile> ISSUED = new ConcurrentHashMap<>();

	private final String id;
	private final String label;

	public MockSocialProvider(String id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public String id() { return id; }

	@Override
	public String label() { return label; }

	@Override
	public String authorizeUrl(String contextPath, String state, String callbackUrl) {
		return contextPath + "/oauth/mock?provider=" + id.toLowerCase() + "&state=" + enc(state) + "&redirect_uri="
				+ enc(callbackUrl);
	}

	/** 목업 로그인 화면에서 "계속하기"를 누르면 호출된다 (실제로는 제공자 서버가 하는 일) */
	public String issueCode(String name, String email) {
		String code = UUID.randomUUID().toString();
		ISSUED.put(code, new SocialProfile(id, stableId(email), name, email));
		return code;
	}

	@Override
	public SocialProfile fetchProfile(String code, String callbackUrl) {
		return code == null ? null : ISSUED.remove(code); // 한 번 쓰면 사라진다
	}

	/** 같은 이메일이면 같은 사용자로 보이도록 고정 ID를 만든다 */
	private static String stableId(String email) {
		try {
			byte[] h = MessageDigest.getInstance("SHA-256").digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
			return "mock-" + HexFormat.of().formatHex(h, 0, 8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
