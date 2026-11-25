package com.market.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.market.util.MiniJson;

/**
 * 실제 OAuth 2.0 인가 코드 방식 제공자의 공통 부분.
 *  1) authorizeUrl : 제공자 로그인·동의 화면 주소 (client_id, redirect_uri, state, scope)
 *  2) fetchProfile : code → (POST) 액세스 토큰 → (GET, Bearer) 사용자 정보 JSON
 * 제공자마다 다른 것은 주소와 사용자 정보 JSON 모양뿐이라 그 부분만 자식 클래스가 정한다.
 * 액세스 토큰은 사용자 정보를 받은 뒤 바로 버린다 (저장하지 않음).
 */
public abstract class OAuth2Provider implements SocialProvider {
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	protected final String clientId;
	protected final String clientSecret;
	private final String authUrl;
	private final String tokenUrl;
	private final String userUrl;

	protected OAuth2Provider(String clientId, String clientSecret, String authUrl, String tokenUrl, String userUrl) {
		this.clientId = clientId;
		this.clientSecret = clientSecret == null ? "" : clientSecret;
		this.authUrl = authUrl;
		this.tokenUrl = tokenUrl;
		this.userUrl = userUrl;
	}

	/** 요청할 권한 (없으면 null → 콘솔에서 설정한 동의 항목을 따른다) */
	protected String scope() {
		return null;
	}

	/** 사용자 정보 JSON → 우리 앱이 쓰는 SocialProfile */
	protected abstract SocialProfile toProfile(Map<String, Object> me);

	@Override
	public String authorizeUrl(String contextPath, String state, String callbackUrl) {
		Map<String, String> q = new LinkedHashMap<>();
		q.put("response_type", "code");
		q.put("client_id", clientId);
		q.put("redirect_uri", callbackUrl);
		q.put("state", state);
		if (scope() != null)
			q.put("scope", scope());
		return authUrl + "?" + form(q);
	}

	@Override
	public SocialProfile fetchProfile(String code, String state, String callbackUrl) {
		if (code == null || code.isBlank())
			return null;
		try {
			// 1) code → 액세스 토큰
			Map<String, String> f = new LinkedHashMap<>();
			f.put("grant_type", "authorization_code");
			f.put("client_id", clientId);
			if (!clientSecret.isEmpty())
				f.put("client_secret", clientSecret);
			f.put("redirect_uri", callbackUrl);
			f.put("code", code);
			f.put("state", state); // 네이버는 토큰 요청에도 state가 필요하다 (다른 곳은 무시)
			HttpRequest tokenReq = HttpRequest.newBuilder(URI.create(tokenUrl)).timeout(Duration.ofSeconds(8))
					.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(form(f))).build();
			HttpResponse<String> tr = HTTP.send(tokenReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Map<String, Object> token = MiniJson.obj(MiniJson.parse(tr.body()));
			String accessToken = MiniJson.str(token, "access_token");
			if (tr.statusCode() != 200 || accessToken.isEmpty()) {
				System.err.println("[OAuth " + id() + "] 토큰 응답 " + tr.statusCode() + ": " + tr.body());
				return null;
			}
			// 2) 액세스 토큰 → 사용자 정보
			HttpRequest meReq = HttpRequest.newBuilder(URI.create(userUrl)).timeout(Duration.ofSeconds(8))
					.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").GET().build();
			HttpResponse<String> mr = HTTP.send(meReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (mr.statusCode() != 200) {
				System.err.println("[OAuth " + id() + "] 사용자 정보 응답 " + mr.statusCode() + ": " + mr.body());
				return null;
			}
			SocialProfile p = toProfile(MiniJson.obj(MiniJson.parse(mr.body())));
			return p == null || p.providerId().isEmpty() ? null : p;
		} catch (Exception e) { // 네트워크 오류, JSON 모양이 다름 등
			System.err.println("[OAuth " + id() + "] " + e);
			return null;
		}
	}

	protected static String form(Map<String, String> m) {
		StringJoiner j = new StringJoiner("&");
		m.forEach((k, v) -> j.add(k + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8)));
		return j.toString();
	}

	/** 이름이 비어 있으면 "카카오 회원"처럼 */
	protected String nameOr(String name) {
		return name == null || name.isBlank() ? label() + " 회원" : name.trim();
	}
}
