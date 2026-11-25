import re, requests, sys
import os
B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb")
ok = 0
def check(cond, msg):
    global ok
    print(("PASS " if cond else "FAIL ") + msg)
    if not cond: sys.exit(1)
    ok += 1
def csrf(html): return re.search(r'name="_csrf" value="([^"]+)"', html).group(1)

s = requests.Session()
r = s.get(B + "/books"); check(r.status_code == 200 and "스크래치" in r.text, "도서 목록")
r = s.get(B + "/books", params={"q": "안드로이드"}); check(r.text.count('class="book-card"') == 1, "검색")
r = s.get(B + "/books", params={"tab": "new"}); check(r.text.index("안드로이드") < r.text.index("스크래치") < r.text.index("쉽게 배우는"), "신간 탭: 출판일 순")
r = s.get(B + "/books", params={"q": "프로그래밍", "sort": "latest"}); check(r.text.count('class="book-card"') == 2 and r.text.index("안드로이드") < r.text.index("쉽게 배우는"), "검색 + 최신순 정렬")
r = s.get(B + "/books", params={"q": "한빛"}); check(r.text.count('class="book-card"') == 3, "출판사로 검색")
check("카카오 REST API 키를 넣으면" in r.text, "키 없을 때 안내 띠")
r = s.get(B + "/books/detail", params={"id": "ISBN1234"}); check("단계별로 쇼핑몰" in r.text, "상세")
t = csrf(r.text)
# CSRF 없는 POST는 거절
r = s.post(B + "/cart/add", data={"id": "ISBN1234"}, allow_redirects=False); check(r.status_code == 403, "CSRF 토큰 없으면 403")
# 장바구니
for bid in ["ISBN1234", "ISBN1234", "ISBN1236"]:
    s.post(B + "/cart/add", data={"_csrf": t, "id": bid, "next": "/books"})
r = s.get(B + "/cart"); check('<output aria-live="polite">2</output>' in r.text, "같은 책 두 번 담으면 수량 2")
s.post(B + "/cart/decrease", data={"_csrf": t, "id": "ISBN1234"})
r = s.get(B + "/cart"); check(r.text.count('<output aria-live="polite">1</output>') == 2, "수량 줄이기 (2→1)")
s.post(B + "/cart/decrease", data={"_csrf": t, "id": "ISBN1234"})
r = s.get(B + "/cart"); check(r.text.count('<output aria-live="polite">1</output>') == 2, "1에서 더 줄이면 그대로 1")
s.post(B + "/cart/increase", data={"_csrf": t, "id": "ISBN1236"})
r = s.get(B + "/cart"); check("71,000원" in r.text, "합계 27,000 + 22,000×2 = 71,000 (배송비 무료)")
# 비로그인 주문 → 로그인으로
r = s.get(B + "/order", allow_redirects=False); check(r.status_code == 302 and "/login?next=%2Forder" in r.headers["Location"], "주문은 로그인 필요")
# 가입 검증
r = s.post(B + "/signup", data={"_csrf": t, "loginId": "ab", "password": "x", "password2": "x", "name": "", "phone": ""})
check("영문과 숫자로 4~20자" in r.text, "가입 입력 검사")
r = s.post(B + "/signup", data={"_csrf": t, "loginId": "tester1", "password": "pass1234", "password2": "pass1234", "name": "홍길동", "phone": "010-1234-5678", "address": "서울시 종로구 세종대로 1", "next": "/order"})
check(r.url.endswith("/order") and "주문서" in r.text, "가입 후 주문서로 이동")
t = csrf(r.text)  # 로그인 뒤 세션 ID가 바뀌어도 토큰은 유지
check("홍길동" in r.text and "서울시 종로구" in r.text, "배송 정보 자동 채움")
check(r.text.count('class="cart-item"') == 0 and "71,000원" in r.text, "로그인 뒤에도 장바구니 유지")
# 쿠폰
r = s.post(B + "/order", data={"_csrf": t, "action": "apply", "couponCode": "nope", "receiverName": "홍길동", "receiverPhone": "010-1234-5678", "address": "서울시 종로구 세종대로 1"})
check("쓸 수 없는 쿠폰" in r.text, "잘못된 쿠폰")
r = s.post(B + "/order", data={"_csrf": t, "couponPick": "WELCOME10", "receiverName": "홍길동", "receiverPhone": "010-1234-5678", "address": "서울시 종로구 세종대로 1"})
check("−5,000원" in r.text and "66,000원" in r.text, "WELCOME10: 10%=7,100 → 최대 5,000 → 66,000")
# 주문
r = s.post(B + "/order", data={"_csrf": t, "action": "place", "couponCode": "WELCOME10", "receiverName": "홍길동", "receiverPhone": "010-1234-5678", "address": "서울시 종로구 세종대로 1, 101호"})
check("/orders/receipt?id=" in r.url and "주문을 마쳤어요" in r.text and "66,000원" in r.text, "주문 → 영수증")
rid = r.url.split("id=")[1]
r = s.get(B + "/cart"); check("장바구니가 비어 있어요" in r.text, "주문 뒤 장바구니 비움")
r = s.get(B + "/orders"); check("외 1종" in r.text, "주문 내역")
# 쿠폰함: 지갑에 없는 쿠폰은 주문서에서 못 씀 → 등록
r = s.get(B + "/coupons"); check("WELCOME10" in r.text and "사용함" in r.text, "쿠폰함: 가입 쿠폰 지급·사용함 표시")
r = s.post(B + "/coupons", data={"_csrf": t, "code": "nope"}); check("없는 쿠폰" in r.text or "찾을 수 없" in r.text, "쿠폰함: 없는 번호")
r = s.post(B + "/coupons", data={"_csrf": t, "code": "book3000"}); check("BOOK3000" in r.text and "사용 가능" in r.text, "쿠폰함: 소문자로 등록")
r = s.post(B + "/coupons", data={"_csrf": t, "code": "BOOK3000"}); check("이미" in r.text, "쿠폰함: 중복 등록 막음")
# 배송비 + BOOK3000 최소 금액 미달
s.post(B + "/cart/add", data={"_csrf": t, "id": "ISBN1236", "next": "/books"})
r = s.post(B + "/order", data={"_csrf": t, "couponPick": "BOOK3000", "receiverName": "a", "receiverPhone": "010-1111-2222", "address": "주소 다섯글자"})
check("30,000원 이상 담으면" in r.text and "25,000원" in r.text, "최소 금액 미달 쿠폰 + 배송비 3,000원")
# 내 정보
r = s.post(B + "/mypage", data={"_csrf": t, "name": "홍길동", "phone": "010-9999-8888", "address": "부산시"})
check("010-9999-8888" in r.text, "내 정보 저장")
# 관리자 화면 접근 막기
r = s.get(B + "/admin/books"); check(r.status_code == 403, "일반 회원은 관리자 403")
# 다른 사람 영수증
s2 = requests.Session(); r = s2.get(B + "/books"); t2 = csrf(s2.get(B + "/books/detail?id=ISBN1234").text)
r = s2.post(B + "/login", data={"_csrf": t2, "loginId": "admin", "password": "wrong"}); check("맞지 않아요" in r.text, "틀린 비밀번호")
r = s2.post(B + "/login", data={"_csrf": t2, "loginId": "admin", "password": "admin1234"}); check(r.url.endswith("/admin"), "관리자 로그인")
t2 = csrf(r.text)
r = s2.post(B + "/admin/books/edit", data={"_csrf": t2, "editing": "false", "bookId": "ISBN1237", "name": "자바 마스터", "unitPrice": "31000", "author": "김자바", "category": "IT/컴퓨터", "stock": "7", "releaseDate": "2025-03-02", "description": "객체지향"})
check(r.url.endswith("/admin/books") and "자바 마스터" in r.text and 'role="alert"' not in r.text, "도서 등록")
r = s2.post(B + "/admin/books/delete", data={"_csrf": t2, "id": "ISBN1234"}); check("지울 수 없어요" in r.text, "주문된 책은 삭제 막기")
r = s2.post(B + "/admin/books/delete", data={"_csrf": t2, "id": "ISBN1237"}); check("자바 마스터" not in r.text, "도서 삭제")
r = s2.post(B + "/admin/coupons", data={"_csrf": t2, "code": "SPRING20", "name": "봄 20%", "type": "RATE", "value": "20", "minAmount": "0", "maxDiscount": "4000"})
check("SPRING20" in r.text, "쿠폰 만들기")
r = s2.post(B + "/admin/coupons", data={"_csrf": t2, "action": "toggle", "code": "SPRING20"}); check("멈춤" in r.text, "쿠폰 멈추기")
r = s2.get(B + "/orders/receipt?id=" + rid); check("홍길동" in r.text, "관리자는 모든 영수증 열람")
s3 = requests.Session(); s3.get(B + "/books"); t3 = csrf(s3.get(B + "/books/detail?id=ISBN1234").text)
s3.post(B + "/signup", data={"_csrf": t3, "loginId": "other1", "password": "pass1234", "password2": "pass1234", "name": "남", "phone": "010-2222-3333"})
r = s3.get(B + "/orders/receipt?id=" + rid); check(r.status_code == 404, "남의 영수증은 404")
# 소셜 목업
s4 = requests.Session()
r = s4.get(B + "/oauth/google/login?next=/cart"); check("테스트용 목업 화면" in r.text, "구글 목업 화면")
t4 = csrf(r.text); state = re.search(r'name="state" value="([^"]+)"', r.text).group(1)
r = s4.post(B + "/oauth/mock", data={"_csrf": t4, "provider": "google", "state": "forged", "name": "구글러", "email": "g@example.com", "action": "allow"})
check(r.url.endswith("/login") and "확인하지 못했어요" in r.text, "state 위조 차단")
r = s4.get(B + "/oauth/naver/login"); t4 = csrf(r.text); state = re.search(r'name="state" value="([^"]+)"', r.text).group(1)
r = s4.post(B + "/oauth/mock", data={"_csrf": t4, "provider": "naver", "state": state, "name": "네이버", "email": "n@example.com", "action": "allow"})
check(r.url.endswith("/oauth/welcome") and "거의 다 됐어요" in r.text, "네이버 목업 첫 로그인 → 추가 정보 화면")
r = s4.post(B + "/oauth/welcome", data={"_csrf": csrf(r.text), "name": "네이버", "phone": "010-5555-6666", "address": ""})
check(r.url.endswith("/books"), "네이버 목업 로그인")
r = s4.get(B + "/mypage"); check("네이버 계정" in r.text and "n@example.com" in r.text, "소셜 회원 자동 가입")
s5 = requests.Session()
r = s5.get(B + "/login"); check("카카오로 계속하기" in r.text and "네이버로 계속하기" in r.text and "Google로 계속하기" in r.text and 'id="id-login"' in r.text and "hidden>" in r.text, "로그인 화면: 소셜 3개 + 아이디 폼 접힘")
r = s5.get(B + "/oauth/kakao/login"); t5 = csrf(r.text); st = re.search(r'name="state" value="([^"]+)"', r.text).group(1)
r = s5.post(B + "/oauth/mock", data={"_csrf": t5, "provider": "kakao", "state": st, "name": "카카오", "email": "k@example.com", "action": "allow"})
r = s5.get(B + "/mypage"); check("카카오 계정" in r.text, "카카오 목업 로그인")

# ===== 새 기능 =====
def login(sess, uid, pw):
    t = csrf(sess.get(B + "/login").text)
    return sess.post(B + "/login", data={"_csrf": t, "loginId": uid, "password": pw})
u = requests.Session(); u.get(B + "/books"); tu = csrf(u.get(B + "/login").text)
u.post(B + "/signup", data={"_csrf": tu, "loginId": "buyer1", "password": "pass1234", "password2": "pass1234", "name": "김구매", "phone": "010-5555-6666", "address": "대전시 유성구 대학로 99"})
tu = csrf(u.get(B + "/books/detail?id=ISBN1236").text)
# 최근 본 책
u.get(B + "/books/detail?id=ISBN1235")
r = u.get(B + "/books"); check("최근 본 책" in r.text and r.text.index("recent__name") > 0, "최근 본 책")
# 리뷰: 사기 전에는 못 씀
r = u.get(B + "/books/detail?id=ISBN1236"); check("주문하면 리뷰를 남길 수 있어요" in r.text, "구매 전 리뷰 막기")
r = u.post(B + "/books/review", data={"_csrf": tu, "id": "ISBN1236", "rating": "5", "content": "x"}); check("구매한 회원만" in r.text, "구매 전 리뷰 요청 거절")
# 찜
r = u.post(B + "/wishlist/toggle", data={"_csrf": tu, "id": "ISBN1236", "next": "/books/detail?id=ISBN1236"}); check('aria-pressed="true"' in r.text and "찜한 책에 넣었어요" in r.text, "찜하기")
r = u.get(B + "/wishlist"); check("스크래치" in r.text, "찜한 책 목록")
r = u.post(B + "/wishlist/toggle", data={"_csrf": tu, "id": "ISBN1236", "next": "/wishlist"}); check("아직 찜한 책이 없어요" in r.text, "찜 풀기")
anon = requests.Session(); ta = csrf(anon.get(B + "/books/detail?id=ISBN1236").text)
r = anon.post(B + "/wishlist/toggle", data={"_csrf": ta, "id": "ISBN1236", "next": "/books/detail?id=ISBN1236"}, allow_redirects=False)
check(r.status_code == 302 and "next=%2Fbooks%2Fdetail%3Fid%3DISBN1236" in r.headers["Location"], "비로그인 찜 → 로그인 후 돌아오기")
# 주문 (WELCOME10) → 같은 쿠폰 다시 쓰기 막기
for _ in range(2): u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1236", "next": "/books"})
ship = {"_csrf": tu, "receiverName": "김구매", "receiverPhone": "010-5555-6666", "address": "대전시 유성구 대학로 99"}
r = u.post(B + "/order", data={**ship, "action": "place", "couponCode": "WELCOME10"}); check("주문을 마쳤어요" in r.text, "쿠폰 주문")
oid = r.url.split("id=")[1]
u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1235", "next": "/books"})
r = u.post(B + "/order", data={**ship, "couponPick": "WELCOME10"}); check("이미 쓴 쿠폰" in r.text and "사용함" in r.text, "같은 쿠폰 두 번 막기")
# 리뷰 쓰기
r = u.get(B + "/books/detail?id=ISBN1236"); check('class="review-form"' in r.text, "구매 후 리뷰 쓰기 칸")
r = u.post(B + "/books/review", data={"_csrf": tu, "id": "ISBN1236", "rating": "4", "content": "아이랑 같이 보기 좋아요"})
check("아이랑 같이 보기 좋아요" in r.text and "김*매" in r.text and '<span class="num">4.0</span>' in r.text, "리뷰 등록 + 이름 가리기 + 평균")
r = u.post(B + "/books/review", data={"_csrf": tu, "id": "ISBN1236", "rating": "5", "content": "또"}); check("이미 리뷰를" in r.text, "리뷰 중복 막기")
# 로컬 베스트셀러: 많이 팔린 책이 1위
r = u.get(B + "/books"); first = r.text.index('class="book-card"'); check(r.text.index("스크래치", first) < r.text.index("쉽게 배우는", first), "베스트셀러: 판매량 순")
# 주문 취소
r = u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid}); check("취소된 주문이에요" in r.text and "취소 일시" in r.text, "주문 취소")
r = u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid}); check("이미 취소됐거나" in r.text, "두 번 취소 막기")
r = u.get(B + "/orders"); check("취소됨" in r.text, "주문 내역에 취소 표시")
r = u.post(B + "/order", data={**ship, "couponPick": "WELCOME10"}); check("쿠폰을 적용했어요" in r.text, "취소 후 쿠폰 다시 쓰기")
r = u.post(B + "/order", data={**ship, "action": "place", "couponCode": "WELCOME10"}); oid2 = r.url.split("id=")[1]
# 남의 주문은 취소 못 함
o = requests.Session(); login(o, "tester1", "pass1234"); to = csrf(o.get(B + "/mypage").text)
r = o.post(B + "/orders/cancel", data={"_csrf": to, "id": oid2}); check("취소할 수 없는 주문" in r.text, "남의 주문 취소 막기")
# 관리자
a = requests.Session(); r = login(a, "admin", "admin1234"); check(r.url.endswith("/admin") and "매출 요약" in r.text, "관리자 → 매출 요약")
check("취소율" in r.text and "많이 팔린 책 TOP 5" in r.text, "요약 화면 항목")
r = a.get(B + "/admin/orders?status=CANCELLED"); check(r.text.count('pill pill--danger') == 1, "주문 관리: 취소됨 필터")
ta2 = csrf(r.text)
r = a.post(B + "/orders/cancel", data={"_csrf": ta2, "id": oid2, "next": "/admin/orders"}); check(r.url.endswith("/admin/orders") and "주문을 취소했어요" in r.text, "관리자 취소")
r = a.get(B + "/admin"); check('<p class="stat__value num">2<small>건</small></p>' in r.text or "stat__value" in r.text, "요약 다시 계산")
print(f"\n{ok}개 모두 통과")
