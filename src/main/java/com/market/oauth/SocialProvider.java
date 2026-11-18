package com.market.oauth;

/**
 * 소셜 로그인(OAuth 2.0 인가 코드 방식) 제공자.
 * 지금은 MockSocialProvider만 있고, 실제 구글/네이버로 바꿀 때는
 * 이 인터페이스를 구현한 클래스를 만들어 SocialProviders에 등록하면 된다.
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
	SocialProfile fetchProfile(String code, String callbackUrl);
}
