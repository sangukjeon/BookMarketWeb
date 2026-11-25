package com.market.oauth;

import java.util.Map;

import com.market.util.MiniJson;

/**
 * 네이버 로그인 (https://developers.naver.com)
 *  인가  https://nid.naver.com/oauth2.0/authorize
 *  토큰  https://nid.naver.com/oauth2.0/token      (client_secret 필수)
 *  정보  https://openapi.naver.com/v1/nid/me       {"resultcode":"00","response":{"id":..,"name":..,"nickname":..,"email":..}}
 * 앱이 "개발 중" 상태면 콘솔에 등록한 테스트 아이디로만 로그인된다.
 */
public class NaverOAuthProvider extends OAuth2Provider {
	public NaverOAuthProvider(String clientId, String clientSecret, String authBase, String apiBase) {
		super(clientId, clientSecret, authBase + "/oauth2.0/authorize", authBase + "/oauth2.0/token", apiBase + "/v1/nid/me");
	}

	@Override public String id() { return "NAVER"; }
	@Override public String label() { return "네이버"; }

	@Override
	protected SocialProfile toProfile(Map<String, Object> me) {
		if (!(me.get("response") instanceof Map))
			return null;
		Map<String, Object> r = MiniJson.obj(me.get("response"));
		String name = MiniJson.str(r, "name");
		if (name.isEmpty())
			name = MiniJson.str(r, "nickname");
		return new SocialProfile(id(), MiniJson.idStr(r, "id"), nameOr(name), MiniJson.str(r, "email"));
	}
}
