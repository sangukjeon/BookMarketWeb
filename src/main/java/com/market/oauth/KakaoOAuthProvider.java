package com.market.oauth;

import java.util.Map;

import com.market.util.MiniJson;

/**
 * 카카오 로그인 (https://developers.kakao.com)
 *  인가  https://kauth.kakao.com/oauth/authorize
 *  토큰  https://kauth.kakao.com/oauth/token      (Client Secret을 켰을 때만 client_secret 필요)
 *  정보  https://kapi.kakao.com/v2/user/me         {"id":123, "kakao_account":{"email":..,"profile":{"nickname":..}}}
 * 이메일은 콘솔의 동의 항목에서 켜고 사용자가 동의했을 때만 온다.
 */
public class KakaoOAuthProvider extends OAuth2Provider {
	public KakaoOAuthProvider(String clientId, String clientSecret, String authBase, String apiBase) {
		super(clientId, clientSecret, authBase + "/oauth/authorize", authBase + "/oauth/token", apiBase + "/v2/user/me");
	}

	@Override public String id() { return "KAKAO"; }
	@Override public String label() { return "카카오"; }

	@Override
	protected SocialProfile toProfile(Map<String, Object> me) {
		Map<String, Object> account = me.get("kakao_account") instanceof Map ? MiniJson.obj(me.get("kakao_account")) : Map.of();
		Map<String, Object> profile = account.get("profile") instanceof Map ? MiniJson.obj(account.get("profile")) : Map.of();
		Map<String, Object> props = me.get("properties") instanceof Map ? MiniJson.obj(me.get("properties")) : Map.of();
		String name = MiniJson.str(profile, "nickname");
		if (name.isEmpty())
			name = MiniJson.str(props, "nickname");
		return new SocialProfile(id(), MiniJson.idStr(me, "id"), nameOr(name), MiniJson.str(account, "email"));
	}
}
