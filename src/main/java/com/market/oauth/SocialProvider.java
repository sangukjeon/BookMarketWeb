package com.market.oauth;

/**
 * 소셜 로그인(OAuth 2.0 인가 코드 방식) 제공자.
 * 구현: OAuth2Provider(Kakao·Naver·Google 실제 연동) / MockSocialProvider(키가 없을 때 목업).
 * 어느 쪽을 쓸지는 SocialProviders가 oauth.properties를 보고 정한다.
 *
 *  1. authorizeUrl  : 사용자를 제공자의 로그인 화면으로 보낼 주소
 *  2. (사용자가 로그인·동의하면 제공자가 callbackUrl?code=...&state=... 로 돌려보냄)
 *  3. fetchProfile  : code로 토큰을 받고, 토큰으로 사용자 정보를 받아 온다
 */
public interface SocialProvider {
	/** GOOGLE / NAVER / KAKAO */
	String id();

	String label();

	String authorizeUrl(String contextPath, String state, String callbackUrl);

	/** code가 틀렸거나 만료됐으면 null */
	SocialProfile fetchProfile(String code, String state, String callbackUrl);
}
