package com.market.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import com.market.dto.Book;
import com.market.util.MiniJson;

/**
 * 카카오 책 검색 API (Daum 검색)
 * GET https://dapi.kakao.com/v3/search/book?query=검색어&sort=accuracy|latest&page=1&size=12
 * 헤더: Authorization: KakaoAK {REST API 키}
 *
 * 알라딘 OpenAPI가 2026-11-01에 서비스를 끝내서 이 클라이언트로 바꿨다.
 * 카카오는 검색만 제공하므로 베스트셀러·신간 목록은 우리 DB(BookDAO.browse)가 맡는다.
 */
public class KakaoBookClient {
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
	/** 카카오는 page 최대 50까지 허용 */
	private static final int MAX_PAGE = 50;
	private final String restKey;
	private final String baseUrl;
	private final int pageSize;

	public KakaoBookClient() {
		Properties p = new Properties();
		try (InputStream in = KakaoBookClient.class.getClassLoader().getResourceAsStream("api.properties")) {
			if (in != null)
				p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (IOException e) {
			// 설정 파일이 없으면 키 없음으로 본다
		}
		this.restKey = p.getProperty("kakao.restKey", "").trim();
		this.baseUrl = p.getProperty("kakao.baseUrl", "https://dapi.kakao.com").trim();
		this.pageSize = Integer.parseInt(p.getProperty("book.pageSize", "12").trim());
	}

	public boolean isConfigured() { return !restKey.isEmpty(); }

	public int pageSize() { return pageSize; }

	/** sort: accuracy(정확도) / latest(최신) */
	public BookPage search(String keyword, String sort, int page) throws IOException, InterruptedException {
		page = Math.min(Math.max(1, page), MAX_PAGE);
		String url = baseUrl + "/v3/search/book?query=" + enc(keyword) + "&sort="
				+ ("latest".equals(sort) ? "latest" : "accuracy") + "&page=" + page + "&size=" + pageSize;
		HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(6))
				.header("Authorization", "KakaoAK " + restKey).GET().build();
		HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (resp.statusCode() != 200) {
			// 예: 401 {"errorType":"AccessDeniedError","message":"..."} — 키가 틀렸거나 앱 설정 문제
			System.err.println("[KakaoBookClient] 응답 " + resp.statusCode() + ": " + resp.body());
			throw new IOException("카카오 응답 코드 " + resp.statusCode());
		}
		Map<String, Object> root = MiniJson.obj(MiniJson.parse(resp.body()));
		Map<String, Object> meta = MiniJson.obj(root.get("meta"));

		BookPage result = new BookPage();
		result.setSource("KAKAO");
		result.setPage(page);
		int pageable = Math.min(MiniJson.num(meta, "pageable_count"), MAX_PAGE * pageSize);
		result.setTotal(MiniJson.num(meta, "total_count"));
		result.setTotalPages((pageable + pageSize - 1) / pageSize);
		for (Object o : MiniJson.arr(root.get("documents"))) {
			Book b = toBook(MiniJson.obj(o));
			if (b != null)
				result.getBooks().add(b);
		}
		return result;
	}

	/** 카카오 문서 → Book. ISBN이 없으면 주문·찜에 쓸 키가 없어 건너뛴다 */
	static Book toBook(Map<String, Object> d) {
		String isbn = "";
		for (String part : MiniJson.str(d, "isbn").trim().split("\\s+"))
			if (part.length() == 13 || (isbn.isEmpty() && part.length() == 10))
				isbn = part; // "8996991341 9788996991342" → 13자리 우선
		if (isbn.isEmpty())
			return null;
		int price = MiniJson.num(d, "sale_price");
		if (price <= 0)
			price = MiniJson.num(d, "price");
		String date = MiniJson.str(d, "datetime");
		date = date.length() >= 10 ? date.substring(0, 10).replace('-', '/') : "";
		String thumb = MiniJson.str(d, "thumbnail");

		Book b = new Book(isbn, cut(MiniJson.str(d, "title"), 200), Math.max(price, 0),
				cut(join(d.get("authors")), 200), cut(MiniJson.str(d, "contents"), 1000), null, date,
				thumb.isEmpty() ? null : cut(thumb, 300));
		b.setPublisher(cut(MiniJson.str(d, "publisher"), 100));
		b.setSource("KAKAO");
		return b;
	}

	private static String join(Object arr) {
		StringBuilder sb = new StringBuilder();
		for (Object o : MiniJson.arr(arr)) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(o);
		}
		return sb.toString();
	}

	private static String cut(String s, int max) {
		return s.length() <= max ? s : s.substring(0, max);
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
