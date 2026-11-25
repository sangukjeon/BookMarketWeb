package com.market.oauth;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * 제공자 목록. 클래스패스의 oauth.properties에 클라이언트 ID가 있으면 실제 OAuth 제공자를,
 * 없으면 목업(MockSocialProvider)을 쓴다. 컨트롤러(SocialLoginServlet)는 어느 쪽인지 몰라도 된다.
 * (주소 값 *.authBase 등은 테스트용 가짜 서버를 가리킬 때만 바꾼다)
 */
public final class SocialProviders {
	private static final Properties P = load();
	private static final Map<String, SocialProvider> PROVIDERS = Map.of(
			"kakao", has("kakao")
					? new KakaoOAuthProvider(prop("kakao.clientId"), prop("kakao.clientSecret"),
							prop("kakao.authBase", "https://kauth.kakao.com"), prop("kakao.apiBase", "https://kapi.kakao.com"))
					: new MockSocialProvider("KAKAO", "카카오"),
			"naver", has("naver")
					? new NaverOAuthProvider(prop("naver.clientId"), prop("naver.clientSecret"),
							prop("naver.authBase", "https://nid.naver.com"), prop("naver.apiBase", "https://openapi.naver.com"))
					: new MockSocialProvider("NAVER", "네이버"),
			"google", has("google")
					? new GoogleOAuthProvider(prop("google.clientId"), prop("google.clientSecret"),
							prop("google.authUrl", "https://accounts.google.com/o/oauth2/v2/auth"),
							prop("google.tokenUrl", "https://oauth2.googleapis.com/token"),
							prop("google.userUrl", "https://openidconnect.googleapis.com/v1/userinfo"))
					: new MockSocialProvider("GOOGLE", "구글"));

	private SocialProviders() {
	}

	public static SocialProvider get(String key) {
		return key == null ? null : PROVIDERS.get(key.toLowerCase());
	}

	/** 하나라도 목업이면 true (로그인 화면 안내 문구용) */
	public static boolean anyMock() {
		return PROVIDERS.values().stream().anyMatch(p -> p instanceof MockSocialProvider);
	}

	/** 카카오 승인 화면 등에 등록한 Redirect URI와 정확히 같게 쓰고 싶을 때 (예: http://localhost:8080/BookMarketWeb) */
	public static String redirectBase() {
		return prop("oauth.redirectBase");
	}

	private static boolean has(String p) {
		return !prop(p + ".clientId").isEmpty();
	}

	private static String prop(String k) {
		return prop(k, "");
	}

	private static String prop(String k, String def) {
		String v = P.getProperty(k, "").trim();
		return v.isEmpty() ? def : v;
	}

	private static Properties load() {
		Properties p = new Properties();
		try (InputStream in = SocialProviders.class.getClassLoader().getResourceAsStream("oauth.properties")) {
			if (in != null)
				p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (Exception e) {
			System.err.println("[SocialProviders] oauth.properties를 읽지 못해 목업으로 동작해요: " + e);
		}
		return p;
	}
}
