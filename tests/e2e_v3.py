"""v3 기능 테스트: 장르, 재고·품절, 마이페이지(배송지·비밀번호·탈퇴), 쿠폰함, 장바구니 저장.
e2e.py와 같은 조건(톰캣 실행 중, schema.sql 시드)에서 돌려요. 회원은 새로 만들어 쓰고, 바꾼 재고는 끝에 되돌려요."""
import os, random, re, sys, html
import requests

B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb")
ok = 0
def check(cond, msg):
    global ok
    print(("PASS " if cond else "FAIL ") + msg)
    if not cond: sys.exit(1)
    ok += 1
def csrf(h): return re.search(r'name="_csrf" value="([^"]+)"', h).group(1)
def login(sess, uid, pw):
    t = csrf(sess.get(B + "/login").text)
    return sess.post(B + "/login", data={"_csrf": t, "loginId": uid, "password": pw})
def signup(uid, name):
    u = requests.Session(); t = csrf(u.get(B + "/signup").text)
    u.post(B + "/signup", data={"_csrf": t, "loginId": uid, "password": "pass1234", "password2": "pass1234", "name": name, "phone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
    return u, csrf(u.get(B + "/mypage").text)
def book_form(sess, bid):
    h = sess.get(B + "/admin/books/edit", params={"id": bid}).text
    d = {"_csrf": csrf(h), "editing": "true"}
    for m in re.finditer(r'<input[^>]*name="([^"]+)"[^>]*>', h):
        tag = m.group(0); v = re.search(r'value="([^"]*)"', tag)
        if m.group(1) not in ("_csrf", "editing") and 'type="checkbox"' not in tag: d[m.group(1)] = html.unescape(v.group(1)) if v else ""
    d["description"] = html.unescape(re.search(r'<textarea[^>]*name="description"[^>]*>(.*?)</textarea>', h, re.S).group(1))
    sel = re.search(r'<option value="([^"]*)" selected', h); d["category"] = html.unescape(sel.group(1)) if sel else ""
    return d
def set_stock(sess, bid, n):
    d = book_form(sess, bid); d["stock"] = str(n)
    return sess.post(B + "/admin/books/edit", data=d)
def stock_of(bid):
    h = requests.get(B + "/books/detail", params={"id": bid}).text
    m = re.search(r'<dt>재고</dt><dd>(.*?)</dd>', h, re.S); return re.sub(r'<[^>]+>', '', m.group(1)).strip()

sfx = str(random.randint(10000, 99999))
a = requests.Session(); login(a, "admin", "admin1234")

# ---------- 장르 ----------
r = requests.get(B + "/books", params={"genre": "it"})
check(r.text.count('class="book-card"') >= 3 and "IT/컴퓨터 책을 모았어요" in r.text, "장르별 목록: IT/컴퓨터")
check("시/에세이" in r.text and "?genre=novel" in r.text and 'class="count num"' in r.text, "사이드바: 10개 장르 + 권수")
r = requests.get(B + "/books", params={"genre": "travel"})
check("book-card" not in r.text and "아직 이 장르의 책이 없어요" in r.text and "mascot/bear.png" in r.text, "빈 장르: 마스코트 빈 화면")
r = requests.get(B + "/books", params={"genre": "nope"}); check(r.status_code == 200 and "지금 많이 찾는 책이에요" in r.text, "없는 장르는 전체 목록")
r = requests.get(B + "/books/detail", params={"id": "ISBN1234"})
check("같은 장르의 다른 책" in r.text and "ISBN1235" in r.text and "?genre=it" in r.text, "상세: 같은 장르의 다른 책")
check("mascot/favicon-32.png" in r.text and "logo__mark" in r.text, "파비콘 + 로고 마스코트")

# ---------- 재고 ----------
orig = stock_of("ISBN1236"); check(re.fullmatch(r"\d+권", orig) is not None, "상세에 재고 표시")
r = set_stock(a, "ISBN1236", 2); check(r.url.endswith("/admin/books") and "2권 남음" not in r.text and ">2<" in r.text, "관리자: 재고 2로 수정 + 목록 재고 칸")
d = book_form(a, "ISBN1236"); d["stock"] = "-3"; r = a.post(B + "/admin/books/edit", data=d); check("재고는 0~9,999" in r.text, "재고 검사: 음수 거절")
d = book_form(a, "ISBN1236"); d["category"] = "요리"; r = a.post(B + "/admin/books/edit", data=d); check("장르는 목록에서" in r.text, "장르 검사: 목록 밖 거절")
r = requests.get(B + "/books", params={"q": "스크래치"}); check("2권 남음" in r.text, "카드에 '2권 남음'")

u, tu = signup("v3" + sfx, "재고왕" + sfx)
check("WELCOME10" in u.get(B + "/coupons").text, "가입하면 쿠폰함에 WELCOME10")
for i in range(2): u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1236", "next": "/cart"})
r = u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1236", "next": "/cart"}); check("재고가 2권뿐이라 더 담을 수 없어요" in r.text, "재고보다 많이 담기 막음")
r = u.post(B + "/cart/increase", data={"_csrf": tu, "id": "ISBN1236"}); check("재고가 2권뿐이에요" in r.text, "수량 늘리기도 재고까지")
set_stock(a, "ISBN1236", 1)
r = u.get(B + "/cart"); check("재고가 1권뿐이에요. 수량을 줄여 주세요" in r.text and "재고를 확인해 주세요" in r.text, "장바구니: 재고 모자람 경고 + 주문 버튼 막힘")
r = u.post(B + "/order", data={"_csrf": tu, "action": "place", "receiverName": "재고왕", "receiverPhone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
check("재고가 모자라요" in r.text and "/orders/receipt" not in r.url, "주문 시 재고 모자라면 거절(트랜잭션)")
check(stock_of("ISBN1236") == "1권 남았어요", "거절된 주문은 재고 그대로")
set_stock(a, "ISBN1236", 0)
r = requests.get(B + "/books/detail", params={"id": "ISBN1236"}); check("품절이에요" in r.text and 'value="buy"' not in r.text, "품절: 구매 버튼 막힘")
r = u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1236", "next": "/cart"}); check("품절된 책이에요" in r.text, "품절 책 담기 막음")
r = u.get(B + "/cart"); check("품절됐어요" in r.text, "장바구니: 품절 표시")
u.post(B + "/cart/remove", data={"_csrf": tu, "id": "ISBN1236"})
set_stock(a, "ISBN1236", int(orig.rstrip("권")))

# 주문하면 재고 감소, 취소하면 복구
u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1235", "next": "/cart"}); u.post(B + "/cart/increase", data={"_csrf": tu, "id": "ISBN1235"})
before = int(stock_of("ISBN1235").rstrip("권"))
r = u.post(B + "/order", data={"_csrf": tu, "action": "place", "receiverName": "재고왕", "receiverPhone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
oid = r.url.split("id=")[1]
check(stock_of("ISBN1235") == f"{before - 2}권", "주문하면 재고 -2")

# ---------- 장바구니 저장 ----------
u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1234", "next": "/cart"})
pc2 = requests.Session(); r = login(pc2, "v3" + sfx, "pass1234")
r = pc2.get(B + "/cart"); check("ISBN1234" in r.text and "다른 PC에서도" in r.text, "다른 세션에서 로그인해도 장바구니 그대로(DB 저장)")
g = requests.Session(); tg = csrf(g.get(B + "/books/detail", params={"id": "ISBN1236"}).text)
g.post(B + "/cart/add", data={"_csrf": tg, "id": "ISBN1236", "next": "/books"})
t = csrf(g.get(B + "/login").text); g.post(B + "/login", data={"_csrf": t, "loginId": "v3" + sfx, "password": "pass1234"})
r = g.get(B + "/cart"); check("ISBN1234" in r.text and "ISBN1236" in r.text, "비회원 장바구니 + 저장된 장바구니 합치기")

# ---------- 탈퇴 (배송 중 주문 있으면 막힘) ----------
r = u.post(B + "/mypage/withdraw", data={"_csrf": tu, "password": "pass1234"}); check("배송이 끝나지 않은 주문" in r.text, "진행 중 주문 있으면 탈퇴 막음")
u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid})
check(stock_of("ISBN1235") == f"{before}권", "주문 취소하면 재고 복구")

# ---------- 배송지 ----------
r = u.post(B + "/mypage/address", data={"_csrf": tu, "label": "", "receiverName": "재고왕", "phone": "010-5555-1234", "address": "x"}); check('role="alert"' in r.text and "배송지 추가하기" in r.text, "배송지 입력 검사")
u.post(B + "/mypage/address", data={"_csrf": tu, "label": "집", "receiverName": "재고왕", "phone": "010-5555-1234", "address": "대전시 유성구 대학로 99"})
r = u.post(B + "/mypage/address", data={"_csrf": tu, "label": "회사", "receiverName": "재고왕", "phone": "010-2222-3333", "address": "서울시 강남구 테헤란로 1", "makeDefault": "on"})
check("배송지를 저장했어요" in r.text and r.text.count('class="addr"') == 2 and "1/5" not in r.text and "2/5" in r.text, "배송지 2개 저장")
ids = re.findall(r'name="id" value="(\d+)"', r.text[r.text.index('id="addresses"'):])
r = u.get(B + "/mypage"); check('value="서울시 강남구 테헤란로 1"' in r.text, "기본 배송지 → 내 정보 주소에도 반영")
r = u.get(B + "/order"); check(r.text.count("data-addr ") == 2 and "회사" in r.text, "주문서: 저장한 배송지 칩")
r = u.post(B + "/mypage/address/default", data={"_csrf": tu, "id": ids[0]}); check("기본 배송지를 바꿨어요" in r.text, "기본 배송지 바꾸기")
r = u.post(B + "/mypage/address/delete", data={"_csrf": tu, "id": ids[-1]}); check("배송지를 지웠어요" in r.text and r.text.count('class="addr"') == 1, "배송지 지우기")
for i in range(4): r = u.post(B + "/mypage/address", data={"_csrf": tu, "label": f"곳{i}", "receiverName": "재고왕", "phone": "010-5555-1234", "address": "부산시 해운대구 센텀로 5"})
check("5/5" in r.text and "배송지 추가하기" not in r.text, "배송지 최대 5개")
o = requests.Session(); login(o, "admin", "admin1234"); 
r = o.post(B + "/mypage/address/delete", data={"_csrf": csrf(o.get(B + "/mypage").text), "id": ids[0]})
check("재고왕" not in r.text, "남의 배송지는 못 지움")

# ---------- 비밀번호 ----------
r = u.post(B + "/mypage/password", data={"_csrf": tu, "currentPassword": "wrong999", "newPassword": "newpass99", "newPassword2": "newpass99"}); check('role="alert"' in r.text and "비밀번호 바꾸기" in r.text, "지금 비밀번호 틀리면 거절")
r = u.post(B + "/mypage/password", data={"_csrf": tu, "currentPassword": "pass1234", "newPassword": "short", "newPassword2": "short"}); check('role="alert"' in r.text, "새 비밀번호 규칙 검사")
r = u.post(B + "/mypage/password", data={"_csrf": tu, "currentPassword": "pass1234", "newPassword": "newpass99", "newPassword2": "newpass99"}); check("비밀번호를 바꿨어요" in r.text, "비밀번호 바꾸기")
x = requests.Session(); r = login(x, "v3" + sfx, "pass1234"); check("/login" in r.url, "옛 비밀번호로는 로그인 안 됨")
x = requests.Session(); r = login(x, "v3" + sfx, "newpass99"); check("/login" not in r.url, "새 비밀번호로 로그인")

# ---------- 탈퇴 ----------
r = u.post(B + "/mypage/withdraw", data={"_csrf": tu, "password": "pass1234"}); check('role="alert"' in r.text and "회원 탈퇴" in r.text, "탈퇴: 비밀번호 틀리면 거절")
r = u.post(B + "/mypage/withdraw", data={"_csrf": tu, "password": "newpass99"}); check("탈퇴했어요" in r.text, "탈퇴")
r = x.get(B + "/mypage"); check("/login" in r.url, "다른 세션도 바로 로그아웃")
r = login(requests.Session(), "v3" + sfx, "newpass99"); check("/login" in r.url, "탈퇴한 아이디로 로그인 안 됨")
r = a.get(B + "/admin/members", params={"status": "WITHDRAWN"}); check("탈퇴한 회원" in r.text and "탈퇴함" in r.text and "재고왕" not in r.text, "관리자: 탈퇴 필터 + 개인정보 지움")
r = a.post(B + "/mypage/withdraw", data={"_csrf": csrf(a.get(B + "/mypage").text), "password": "admin1234"}); check("관리자" in r.text and "탈퇴했어요" not in r.text, "관리자는 탈퇴 못 함")

# ---------- 관리자 쿠폰 통계 ----------
r = a.get(B + "/admin/coupons"); check("등록 / 사용" in r.text and re.search(r'class="r nowrap num">\d+ / \d+<', r.text), "관리자 쿠폰: 등록·사용 수")

print(f"\n{ok}개 모두 통과")
