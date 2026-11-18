package com.market.oauth;

import java.util.Map;

/**
 * 제공자 목록. 실제 OAuth로 바꿀 때는 여기에서
 *   "google" → new GoogleOAuthProvider(clientId, secret)
 *   "naver"  → new NaverOAuthProvider(clientId, secret)
 *   "kakao"  → new KakaoOAuthProvider(clientId, secret)
 * 처럼 바꿔 끼우면 컨트롤러(SocialLoginServlet)는 고칠 필요가 없다.
 */
public final class SocialProviders {
	private static final Map<String, SocialProvider> PROVIDERS = Map.of(
			"google", new MockSocialProvider("GOOGLE", "구글"),
			"naver", new MockSocialProvider("NAVER", "네이버"),
			"kakao", new MockSocialProvider("KAKAO", "카카오"));

	private SocialProviders() {
	}

	public static SocialProvider get(String key) {
		return key == null ? null : PROVIDERS.get(key.toLowerCase());
	}
}
