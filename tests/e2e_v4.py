"""v4 기능 테스트: 검색 도우미, 함께 산 책, 재입고 알림·알림함, 이번 주 베스트, 윤상문고 추천, 곰 독서 성향 테스트, 출석 체크.
e2e.py와 같은 조건(톰캣 실행 중, schema 시드)에서 돌려요. 바꾼 재고는 끝에 되돌려요.
주간 베스트의 순위 변화와 7일 출석은 날짜를 되돌려야 해서 DB 명령을 같이 써요:
  DB=mysql (기본, `mysql -uroot bookmarket`) / DB=pg (`psql`, PG* 환경 변수) / DB=none (그 부분 건너뜀)"""
import os, random, re, subprocess, sys, json, datetime
import requests

B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb")
DB = os.environ.get("DB", "mysql")
ok = 0
def check(cond, msg):
    global ok
    print(("PASS " if cond else "FAIL ") + msg)
    if not cond: sys.exit(1)
    ok += 1
def csrf(h): return re.search(r'(?:name="_csrf" value|data-csrf)="([^"]+)"', h).group(1)
def login(sess, uid, pw):
    t = csrf(sess.get(B + "/login").text)
    return sess.post(B + "/login", data={"_csrf": t, "loginId": uid, "password": pw})
def signup(uid, name):
    u = requests.Session(); t = csrf(u.get(B + "/signup").text)
    u.post(B + "/signup", data={"_csrf": t, "loginId": uid, "password": "pass1234", "password2": "pass1234", "name": name, "phone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
    return u, csrf(u.get(B + "/mypage").text)
def sql(q):
    if DB == "mysql": cmd = ["mysql", "-uroot", "bookmarket", "-e", q]
    elif DB == "pg": cmd = ["psql", "-q", "-h", os.environ.get("PGHOST", "127.0.0.1"), "-U", os.environ.get("PGUSER", "app"), os.environ.get("PGDATABASE", "bm"), "-c", q]
    else: return False
    subprocess.run(cmd, check=True, capture_output=True); return True
def order(u, t, books):
    for b in books: u.post(B + "/cart/add", data={"_csrf": t, "id": b, "next": "/cart"})
    r = u.post(B + "/order", data={"_csrf": t, "action": "place", "receiverName": "테스트", "receiverPhone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
    return r.url.split("id=")[1] if "id=" in r.url else None
def book_form(sess, bid):
    import html
    h = sess.get(B + "/admin/books/edit", params={"id": bid}).text
    d = {"_csrf": csrf(h), "editing": "true"}
    for m in re.finditer(r'<input[^>]*name="([^"]+)"[^>]*>', h):
        tag = m.group(0); v = re.search(r'value="([^"]*)"', tag)
        if m.group(1) not in ("_csrf", "editing"): d[m.group(1)] = html.unescape(v.group(1)) if v else ""
    d["description"] = html.unescape(re.search(r'<textarea[^>]*name="description"[^>]*>(.*?)</textarea>', h, re.S).group(1))
    sel = re.search(r'<option value="([^"]*)" selected', h); d["category"] = html.unescape(sel.group(1)) if sel else ""
    return d
def set_stock(sess, bid, n):
    d = book_form(sess, bid); d["stock"] = str(n); return sess.post(B + "/admin/books/edit", data=d)
def suggest(sess): return json.loads(sess.get(B + "/search/suggest").text)

sfx = str(random.randint(10000, 99999))
a = requests.Session(); login(a, "admin", "admin1234"); ta = csrf(a.get(B + "/admin").text)

# ---------- 검색 도우미 ----------
g = requests.Session()
g.get(B + "/books", params={"q": "  스크래치   "}); g.get(B + "/books", params={"q": "J"}); g.get(B + "/books", params={"q": "없는책제목" + sfx})
s = suggest(g)
check(s["recent"][:2] == ["없는책제목" + sfx, "스크래치"] and "J" not in s["recent"], "비회원 최근 검색어: 공백 정리, 한 글자는 안 남김")
check("스크래치" in s["popular"] and ("없는책제목" + sfx) not in s["popular"], "인기 검색어: 결과가 있었던 검색만")
r = g.post(B + "/search/recent/delete", data={"_csrf": csrf(g.get(B + "/books").text), "keyword": "스크래치"}, headers={"X-Requested-With": "fetch"})
check(r.status_code == 204 and "스크래치" not in suggest(g)["recent"] and "스크래치" in suggest(g)["popular"], "최근 검색어 지우기 (인기 순위엔 남음)")
u, tu = signup("v4" + sfx, "곰돌" + sfx)
u.get(B + "/books", params={"q": '<b>"안드로이드'}); u.get(B + "/books", params={"q": "JSP"})
raw = u.get(B + "/search/suggest").text
check(json.loads(raw)["recent"][:2] == ["JSP", '<b>"안드로이드'] and "\\u003cb\\u003e" in raw, "회원 최근 검색어(DB, 최근 순) + JSON 이스케이프")
u.post(B + "/search/recent/delete", data={"_csrf": tu}, headers={"X-Requested-With": "fetch"})
check(suggest(u)["recent"] == [], "최근 검색어 전체 지우기")
check('data-suggest="' in u.get(B + "/books").text, "검색창에 도우미 연결")

# ---------- 함께 산 책 · 이번 주 베스트 ----------
o1 = order(u, tu, ["ISBN1234", "ISBN1235"])
h = requests.get(B + "/books/detail", params={"id": "ISBN1234"}).text
i = h.find("이 책을 산 분들이 함께 산 책")
check(i > 0 and "ISBN1235" in h[i:i + 3000], "함께 산 책: 같은 주문의 다른 책 (셀프 조인)")
def together(bid):
    h = requests.get(B + "/books/detail", params={"id": bid}).text
    i = h.find("이 책을 산 분들이 함께 산 책")
    return [] if i < 0 else re.findall(r'detail\?id=([A-Z0-9]+)', h[i:h.find("</section>", i)])
before = together("ISBN1236")
o2 = order(u, tu, ["ISBN1236", "ISBN1234"])
check("ISBN1234" in together("ISBN1236"), "함께 산 책: 주문하면 바로 반영")
u.post(B + "/orders/cancel", data={"_csrf": tu, "id": o2})
check(together("ISBN1236") == before, "함께 산 책: 취소한 주문은 빼요")
h = requests.get(B + "/books").text
check("이번 주 베스트 10" in h and 'class="weekly__rank num">1<' in h and "NEW" in h, "첫 화면: 이번 주 베스트 10 (처음 들어온 책 NEW)")
h = requests.get(B + "/books", params={"wk": "travel"}).text
check("이번 주에 이 장르는 아직 팔린 책이 없어요" in h, "주간 베스트: 장르 탭")
if DB != "none":
    # 지난주에 스크래치 3권이 팔렸던 것으로 → 이번 주(안드로이드·JSP)와 비교하면 스크래치는 빠지고, 순위 변동 표시
    old = order(u, tu, ["ISBN1236", "ISBN1236", "ISBN1236"])
    ago = (datetime.datetime.now() - datetime.timedelta(days=9)).strftime("%Y-%m-%d %H:%M:%S")
    sql(f"UPDATE orders SET ordered_at = '{ago}' WHERE order_id = {old}")
    o3 = order(u, tu, ["ISBN1236"])
    ago2 = (datetime.datetime.now() - datetime.timedelta(days=10)).strftime("%Y-%m-%d %H:%M:%S")
    old2 = order(u, tu, ["ISBN1234"])
    sql(f"UPDATE orders SET ordered_at = '{ago2}' WHERE order_id = {old2}")
    h = requests.get(B + "/books").text
    sec = h[h.index("이번 주 베스트 10"):h.index("weekly__list") + 4000]
    check("rank-move--down" in sec or "rank-move--up" in sec, "주간 베스트: 지난주와 비교한 순위 변동(▲▼)")
else:
    print("SKIP 주간 순위 변동 (DB=none)")

# ---------- 재입고 알림 · 알림함 ----------
orig = int(re.search(r'<dt>재고</dt><dd>(\d+)권', requests.get(B + "/books/detail", params={"id": "ISBN1235"}).text).group(1)) if re.search(r'<dt>재고</dt><dd>(\d+)권', requests.get(B + "/books/detail", params={"id": "ISBN1235"}).text) else 20
set_stock(a, "ISBN1235", 0)
h = u.get(B + "/books/detail", params={"id": "ISBN1235"}).text
check("재입고 알림 받기" in h and 'form="restock-request"' in h and 'value="buy"' not in h, "품절 책: 재입고 알림 받기 버튼")
r = requests.Session().post(B + "/restock", data={"id": "ISBN1235"})
check(r.status_code in (403, 200) and ("/login" in r.url or r.status_code == 403), "비회원은 로그인 필요")
r = u.post(B + "/restock", data={"_csrf": tu, "id": "ISBN1235"})
check("다시 들어오면 알림으로 알려 드릴게요" in r.text and "알림 신청함 · 취소하기" in r.text, "재입고 알림 신청")
u.post(B + "/restock", data={"_csrf": tu, "id": "ISBN1235", "action": "cancel"})
check("재입고 알림 받기" in u.get(B + "/books/detail", params={"id": "ISBN1235"}).text, "신청 취소")
u.post(B + "/restock", data={"_csrf": tu, "id": "ISBN1235"})
r = u.post(B + "/restock", data={"_csrf": tu, "id": "ISBN1234"}); check("지금 재고가 있어서" in r.text, "재고 있는 책은 신청 안 함")
r = set_stock(a, "ISBN1235", orig)
check("재입고 알림 1건을 보냈어요" in r.text, "관리자 재고 채우면 알림 발송")
h = u.get(B + "/books").text
check(re.search(r'aria-label="알림, 안 읽은 알림 (\d+)개"', h) is not None, "상단 종 아이콘에 안 읽은 알림 수")
h = u.get(B + "/notifications").text
check("다시 들어왔어요" in h and "is-new" in h and "/books/detail?id=ISBN1235" in h, "알림함: 재입고 알림 + 링크")
check('안 읽은 알림' not in u.get(B + "/books").text, "열어 보면 읽음 처리")
r = set_stock(a, "ISBN1235", orig); check("재입고 알림" not in r.text, "알림은 한 번만 (신청 지움)")
a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": o1, "from": "ORDERED"})
a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": o1, "from": "PREPARING"})
check("배송이 시작됐어요" in u.get(B + "/notifications").text, "배송 시작 알림")

# ---------- 윤상문고 추천 ----------
r = a.post(B + "/admin/picks", data={"_csrf": ta, "bookId": "NOPE1", "comment": "좋은 책이에요 정말"}); check("맞는 책이 없어요" in r.text, "추천: 없는 도서 ID")
r = a.post(B + "/admin/picks", data={"_csrf": ta, "bookId": "isbn1236", "comment": "짧"}); check("5~100자" in r.text, "추천: 한마디 길이 검사")
r = a.post(B + "/admin/picks", data={"_csrf": ta, "bookId": "isbn1236", "comment": "코딩을 처음 만나는 아이에게 " + sfx})
check("첫 화면에 추천을 올렸어요" in r.text and "첫 화면에 보임" in r.text, "추천 올리기")
h = requests.get(B + "/books").text
check("윤상문고 추천" in h and "코딩을 처음 만나는 아이에게 " + sfx in h, "첫 화면에 추천 + 곰")
pid = re.search(r'name="id" value="(\d+)"', r.text[r.text.index("첫 화면에 보임"):]).group(1)
a.post(B + "/admin/picks/delete", data={"_csrf": ta, "id": pid})
check(("코딩을 처음 만나는 아이에게 " + sfx) not in requests.get(B + "/books").text, "추천 내리기")
check(u.get(B + "/admin/picks").status_code == 403, "추천 관리는 관리자만")

# ---------- 곰 독서 성향 테스트 ----------
g = requests.Session(); tg = csrf(g.get(B + "/taste").text)
r = g.post(B + "/taste", data={"_csrf": tg, "q1": "adventure"}); check("모든 질문에 하나씩" in r.text and 'value="adventure" checked' in r.text, "테스트: 다 안 고르면 다시 (고른 값 유지)")
r = g.post(B + "/taste", data={"_csrf": tg, "q1": "hacker", "q2": "thinker", "q3": "thinker", "q4": "thinker", "q5": "thinker"}); check("모든 질문에 하나씩" in r.text, "테스트: 없는 보기 거절")
ans = {"_csrf": tg, "q1": "maker", "q2": "thinker", "q3": "thinker", "q4": "maker", "q5": "thinker"}
r = g.post(B + "/taste", data=ans); check("/taste/result?type=thinker" in r.url and "생각하는 곰" in r.text and "로그인하고 다시 하면" in r.text, "비회원 결과: 생각하는 곰 + 로그인 안내")
check("?genre=humanities" in r.text and "?genre=science" in r.text, "결과: 어울리는 장르 링크")
ans["_csrf"] = tu
r = u.post(B + "/taste", data=ans); check("500원 쿠폰을 쿠폰함에 넣었어요" in r.text, "회원: 처음 끝내면 500원 쿠폰")
r = u.post(B + "/taste", data={**ans, "q1": "feeler", "q2": "feeler", "q3": "feeler"}); check("감성 곰" in r.text and "500원 쿠폰을 쿠폰함에 넣었어요" not in r.text, "다시 해도 쿠폰은 한 번")
check("TASTE500" in u.get(B + "/coupons").text and "감성 곰" in u.get(B + "/mypage").text, "쿠폰함 + 마이페이지에 내 곰")
r = u.post(B + "/coupons", data={"_csrf": tu, "code": "attend7"}); check("출석으로 받을 수 있어요" in r.text, "이벤트 쿠폰은 번호로 등록 못 함")

# ---------- 출석 체크 ----------
r = u.post(B + "/attendance", data={"_csrf": tu}); check("1일째" in r.text and "오늘 출석 완료" in r.text, "출석하기")
r = u.post(B + "/attendance", data={"_csrf": tu}); check("오늘은 이미 출석했어요" in r.text, "하루 한 번만")
check("is-checked" in r.text and "is-today" in r.text, "달력에 출석 표시")
if DB != "none":
    no = re.search(r'/admin/members/detail\?no=(\d+)', a.get(B + "/admin/members", params={"q": "v4" + sfx}).text).group(1)
    today = datetime.date.today()
    sql(f"DELETE FROM attendance WHERE member_no = {no}")
    sql("INSERT INTO attendance (member_no, check_date) VALUES " + ",".join(f"({no}, '{today - datetime.timedelta(days=d)}')" for d in range(1, 7)))
    r = u.post(B + "/attendance", data={"_csrf": tu}); check("7일 연속 출석이에요" in r.text, "7일 연속 → 1,000원 쿠폰")
    check("ATTEND7" in u.get(B + "/coupons").text and "1,000원 쿠폰이 쿠폰함에" in u.get(B + "/notifications").text, "쿠폰함 + 알림")
else:
    print("SKIP 7일 연속 출석 (DB=none)")

# ---------- 탈퇴하면 같이 지움 ----------
u.post(B + "/orders/cancel", data={"_csrf": tu, "id": o1})
for oid in re.findall(r'/orders/receipt\?id=(\d+)', u.get(B + "/orders").text):
    u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid})
    for st in ["ORDERED", "PREPARING", "SHIPPING"]:
        a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": st})
r = u.post(B + "/mypage/withdraw", data={"_csrf": tu, "password": "pass1234"}); check("탈퇴했어요" in r.text, "v4 기록이 있어도 탈퇴됨 (알림·출석·테스트·검색 기록 정리)")

print(f"\n{ok}개 모두 통과")
