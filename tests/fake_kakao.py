# 카카오 책 검색 응답 형식을 흉내 내는 테스트 서버 (127.0.0.1:8098)
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs
DOCS = []
for i in range(1, 31):
    DOCS.append({"title": f"자바 테스트 도서 {i:02d}", "contents": f"설명 {i} & 소개",
        "url": f"https://search.daum.net/{i}", "isbn": f"89{i:08d} 979110000{i:04d}",
        "datetime": f"2026-{(i % 12) + 1:02d}-{(i % 27) + 1:02d}T00:00:00.000+09:00",
        "authors": [f"저자{i}", "공저자"], "publisher": "테스트출판", "translators": [],
        "price": 20000 + i * 100, "sale_price": (18000 + i * 100) if i % 5 else -1,
        "thumbnail": f"https://search1.kakaocdn.net/thumb/{i}.jpg" if i % 7 else "", "status": "정상판매"})
DOCS.append({"title": "ISBN 없는 책", "contents": "", "url": "x", "isbn": "", "datetime": "", "authors": [],
             "publisher": "", "translators": [], "price": 1000, "sale_price": -1, "thumbnail": "", "status": ""})
for i in range(1, 6):  # 장르 화면용 (장르 낱말 "에세이"로 찾힘)
    DOCS.append({"title": f"여름 에세이 {i}", "contents": "산문", "url": f"https://search.daum.net/e{i}", "isbn": f"979220000{i:04d}",
        "datetime": f"2026-0{i}-01T00:00:00.000+09:00", "authors": ["작가"], "publisher": "산문출판", "translators": [],
        "price": 15000, "sale_price": 13500, "thumbnail": "", "status": "정상판매"})
class H(BaseHTTPRequestHandler):
    def log_message(self, *a): pass
    def do_GET(self):
        if self.headers.get("Authorization") != "KakaoAK TESTKEY":
            self.send_response(401); self.end_headers()
            self.wfile.write(b'{"errorType":"AccessDeniedError","message":"wrong appKey"}'); return
        q = parse_qs(urlparse(self.path).query)
        kw = q.get("query", [""])[0]; size = int(q.get("size", ["10"])[0]); page = int(q.get("page", ["1"])[0])
        items = [d for d in DOCS if kw in d["title"]]
        if q.get("sort", [""])[0] == "latest": items = sorted(items, key=lambda d: d["datetime"], reverse=True)
        part = items[(page - 1) * size: page * size]
        body = {"meta": {"total_count": len(items), "pageable_count": len(items), "is_end": page * size >= len(items)}, "documents": part}
        data = json.dumps(body, ensure_ascii=False).encode()
        self.send_response(200); self.send_header("Content-Type", "application/json;charset=UTF-8"); self.end_headers(); self.wfile.write(data)
HTTPServer(("127.0.0.1", 8098), H).serve_forever()
