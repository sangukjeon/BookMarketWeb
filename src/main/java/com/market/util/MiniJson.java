package com.market.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 라이브러리 없이 JSON을 읽는 작은 파서.
 * 객체 → Map, 배열 → List, 숫자 → Double, 문자열 → String, true/false → Boolean, null → null
 */
public final class MiniJson {
	private final String s;
	private int i;

	private MiniJson(String s) {
		this.s = s;
	}

	public static Object parse(String json) {
		MiniJson p = new MiniJson(json.trim());
		Object v = p.value();
		p.ws();
		if (p.i < p.s.length() && p.s.charAt(p.i) == ';') // 알라딘 응답 끝의 세미콜론
			p.i++;
		return v;
	}

	private void ws() {
		while (i < s.length() && Character.isWhitespace(s.charAt(i)))
			i++;
	}

	private Object value() {
		ws();
		char c = s.charAt(i);
		switch (c) {
		case '{': return object();
		case '[': return array();
		case '"': return string();
		case 't': i += 4; return Boolean.TRUE;
		case 'f': i += 5; return Boolean.FALSE;
		case 'n': i += 4; return null;
		default: return number();
		}
	}

	private Map<String, Object> object() {
		Map<String, Object> m = new LinkedHashMap<>();
		i++;
		ws();
		if (s.charAt(i) == '}') { i++; return m; }
		while (true) {
			ws();
			String key = string();
			ws();
			i++; // :
			m.put(key, value());
			ws();
			char c = s.charAt(i++);
			if (c == '}') return m;
		}
	}

	private List<Object> array() {
		List<Object> list = new ArrayList<>();
		i++;
		ws();
		if (s.charAt(i) == ']') { i++; return list; }
		while (true) {
			list.add(value());
			ws();
			char c = s.charAt(i++);
			if (c == ']') return list;
		}
	}

	private String string() {
		StringBuilder b = new StringBuilder();
		i++; // "
		while (true) {
			char c = s.charAt(i++);
			if (c == '"') return b.toString();
			if (c != '\\') { b.append(c); continue; }
			char e = s.charAt(i++);
			switch (e) {
			case 'n': b.append('\n'); break;
			case 't': b.append('\t'); break;
			case 'r': b.append('\r'); break;
			case 'b': b.append('\b'); break;
			case 'f': b.append('\f'); break;
			case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
			default: b.append(e); // \" \\ \/ 그리고 알라딘이 가끔 쓰는 \'
			}
		}
	}

	private Double number() {
		int start = i;
		while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0)
			i++;
		return Double.valueOf(s.substring(start, i));
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> obj(Object o) { return (Map<String, Object>) o; }

	@SuppressWarnings("unchecked")
	public static List<Object> arr(Object o) { return o == null ? List.of() : (List<Object>) o; }

	public static String str(Map<String, Object> m, String k) {
		Object v = m.get(k);
		return v == null ? "" : v.toString();
	}

	public static int num(Map<String, Object> m, String k) {
		Object v = m.get(k);
		return v instanceof Number n ? n.intValue() : 0;
	}
}
