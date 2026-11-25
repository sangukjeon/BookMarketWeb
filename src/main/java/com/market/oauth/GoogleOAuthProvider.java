package com.market.oauth;

import java.util.Map;

import com.market.util.MiniJson;

/**
 * Google 로그인 (https://console.cloud.google.com → API 및 서비스 → 사용자 인증 정보)
 *  인가  https://accounts.google.com/o/oauth2/v2/auth   (scope: openid email profile)
 *  토큰  https://oauth2.googleapis.com/token
 *  정보  https://openidconnect.googleapis.com/v1/userinfo  {"sub":..,"name":..,"email":..}
 */
public class GoogleOAuthProvider extends OAuth2Provider {
	public GoogleOAuthProvider(String clientId, String clientSecret, String authUrl, String tokenUrl, String userUrl) {
		super(clientId, clientSecret, authUrl, tokenUrl, userUrl);
	}

	@Override public String id() { return "GOOGLE"; }
	@Override public String label() { return "구글"; }

	@Override
	protected String scope() {
		return "openid email profile";
	}

	@Override
	protected SocialProfile toProfile(Map<String, Object> me) {
		return new SocialProfile(id(), MiniJson.idStr(me, "sub"), nameOr(MiniJson.str(me, "name")), MiniJson.str(me, "email"));
	}
}
