import re, requests, sys, subprocess, os
B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb"); ok = 0
def check(c, m):
    global ok
    print(("PASS " if c else "FAIL ") + m)
    if not c: sys.exit(1)
    ok += 1
def csrf(h): return re.search(r'name="_csrf" value="([^"]+)"', h).group(1)
s = requests.Session()
r = s.get(B + "/books"); check("카카오 REST API 키를 넣으면" not in r.text and r.text.count('class="book-card"') == 3, "키 있음: 베스트셀러 탭은 DB 3권")
r = s.get(B + "/books", params={"q": "자바"}); check(r.text.count('class="book-card"') == 12 and "검색 결과 제공: 카카오" in r.text, "카카오 검색 12권")
check('</strong> / 3</span>' in r.text and "저자1, 공저자" in r.text, "페이지 수(31→30권/12) + 저자 여러 명")
check('src="https://search1.kakaocdn.net/thumb/1.jpg"' in r.text, "카카오 표지")
r = s.get(B + "/books", params={"q": "자바", "page": 3}); check(r.text.count('class="book-card"') == 6, "3쪽 6권")
r = s.get(B + "/books", params={"q": "자바", "sort": "latest"}); i = r.text.index('class="book-card"'); check("자바 테스트 도서 11" in r.text[i:i+2500], "최신순 (12월 출판 책이 먼저)")
r = s.get(B + "/books", params={"q": "ISBN 없는"}); check(r.text.count('class="book-card"') == 0, "ISBN 없는 결과는 제외")
# DB 확인: 기본은 MySQL (mysql -N -e). PostgreSQL이면 DB_CLI="psql -At -F, ..." 처럼 바꾸고 BOOK_TABLE=book
cli = os.environ.get("DB_CLI", os.environ.get("MYSQL", "mysql") + " " + os.environ.get("MYSQL_ARGS", "") + " --default-character-set=utf8mb4 -N -e").split()
table = os.environ.get("BOOK_TABLE", "bookmarket.book")
out = re.split(r"[\t|]", subprocess.run(cli + [f"SELECT COUNT(*), MAX(publisher) FROM {table} WHERE source='KAKAO'"], capture_output=True, text=True).stdout.strip())
check(int(out[0]) >= 24 and out[1].strip() == "테스트출판", f"카카오 도서 DB 저장 ({out[0]}권)")
r = s.get(B + "/books/detail", params={"id": "9791100000005"}); check("18,500" not in r.text and "20,500" in r.text and "설명 5 &amp; 소개" in r.text, "할인가 없으면(-1) 정가 + 설명")
r = s.get(B + "/books/detail", params={"id": "9791100000004"}); check("18,400" in r.text, "할인가 있으면 할인가")
t = csrf(r.text); s.post(B + "/cart/add", data={"_csrf": t, "id": "9791100000004", "next": "/books"})
r = s.get(B + "/cart"); check("자바 테스트 도서 04" in r.text, "카카오 책 장바구니")
r = s.get(B + "/books", params={"tab": "new"}); check("자바 테스트 도서" in r.text, "신간 탭에 저장된 카카오 책")
r = s.get(B + "/books", params={"q": "자바", "sort": "sales"}); check("검색 결과 제공: 카카오" not in r.text and r.text.count('class="book-card"') == 12, "판매량순은 DB 검색")
print(f"\n{ok}개 모두 통과")
