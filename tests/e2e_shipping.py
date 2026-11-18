"""배송 단계·취소 제한·별점 입력 테스트. 톰캣 실행 중에 돌려요. 회원·주문을 새로 만들어 쓰므로 DB 초기화는 없어도 돼요."""
import os, random, re, sys
import requests

B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb")
ok = 0
def check(cond, msg):
    global ok
    print(("PASS " if cond else "FAIL ") + msg)
    if not cond: sys.exit(1)
    ok += 1
def csrf(html): return re.search(r'name="_csrf" value="([^"]+)"', html).group(1)
def login(sess, uid, pw):
    t = csrf(sess.get(B + "/login").text)
    return sess.post(B + "/login", data={"_csrf": t, "loginId": uid, "password": pw})

sfx = str(random.randint(10000, 99999))
uid = "ship" + sfx
u = requests.Session(); t = csrf(u.get(B + "/signup").text)
u.post(B + "/signup", data={"_csrf": t, "loginId": uid, "password": "pass1234", "password2": "pass1234", "name": "배송" + sfx, "phone": "010-3333-4444", "address": "인천시 연수구 송도과학로 1"})
tu = csrf(u.get(B + "/mypage").text)
ship = {"_csrf": tu, "receiverName": "배송", "receiverPhone": "010-3333-4444", "address": "인천시 연수구 송도과학로 1"}
def order(book):
    u.post(B + "/cart/add", data={"_csrf": tu, "id": book, "next": "/books"})
    r = u.post(B + "/order", data={**ship, "action": "place"})
    return r.url.split("id=")[1], r
oid, r = order("ISBN1235")
check("ship-steps" in r.text and "is-now" in r.text and "주문완료" in r.text, "영수증: 배송 진행 막대 (주문완료)")
check("주문 취소하기" in r.text, "주문완료 상태에서는 취소 버튼")
oid2, _ = order("ISBN1236")

a = requests.Session(); login(a, "admin", "admin1234")
r = a.get(B + "/admin/orders"); ta = csrf(r.text)
check("배송 준비하기" in r.text and "배송 준비 중" in r.text and "배송 완료" in r.text, "주문 관리: 다음 단계 버튼 + 상태 칩")
r = u.post(B + "/admin/orders/status", data={"_csrf": tu, "id": oid, "from": "ORDERED"}); check(r.status_code == 403, "일반 회원은 상태 변경 403")

# ORDERED → PREPARING
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "ORDERED", "next": "/admin/orders"})
check("배송 준비 중으로 바꿨어요" in r.text, "배송 준비 시작")
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "ORDERED"}); check("이미 상태가 바뀌었거나" in r.text, "같은 버튼 두 번 → 한 번만 바뀜")
r = u.get(B + "/orders/receipt?id=" + oid); check("배송 준비 중" in r.text and "주문 취소하기" in r.text, "배송 준비 중에도 취소 가능")
r = a.get(B + "/admin/orders?status=PREPARING"); check(r.text.count('pill pill--warn') >= 1 and "배송 시작하기" in r.text, "상태 필터: 배송 준비 중")

# PREPARING → SHIPPING
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "PREPARING"}); check("배송 중으로 바꿨어요" in r.text, "배송 시작")
r = u.get(B + "/orders/receipt?id=" + oid)
check("배송이 시작돼서 취소할 수 없어요" in r.text and 'action="/BookMarketWeb/orders/cancel"' not in r.text and "배송 시작</dt>" in r.text, "배송 중: 취소 버튼 없음 + 안내 + 배송 시작 시각")
r = u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid}); check("배송이 시작돼서 취소할 수 없어요" in r.text, "배송 중 회원 취소 거절")
r = a.post(B + "/orders/cancel", data={"_csrf": ta, "id": oid, "next": "/admin/orders"}); check("배송이 시작돼서 취소할 수 없어요" in r.text, "배송 중 관리자 취소도 거절")
r = u.get(B + "/orders"); check("배송 중" in r.text, "주문 내역에 배송 중 표시")

# SHIPPING → DELIVERED, 그 뒤로는 없음
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "SHIPPING"}); check("배송 완료로 바꿨어요" in r.text, "배송 완료 처리")
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "DELIVERED"}); check("바꿀 수 없는 주문" in r.text, "배송 완료 다음 단계 없음")
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid, "from": "CANCELLED"}); check("바꿀 수 없는 주문" in r.text, "엉뚱한 상태 값 거절")
r = u.get(B + "/orders/receipt?id=" + oid); check("배송 완료</dt>" in r.text and r.text.count("is-done") == 4, "영수증: 4단계 모두 완료")

# 취소된 주문은 배송 단계로 못 넘김
r = u.post(B + "/orders/cancel", data={"_csrf": tu, "id": oid2}); check("주문을 취소했어요" in r.text, "주문완료 상태 취소")
r = a.post(B + "/admin/orders/status", data={"_csrf": ta, "id": oid2, "from": "ORDERED"}); check("이미 상태가 바뀌었거나" in r.text, "취소된 주문은 배송 시작 불가")
r = a.get(B + "/admin/orders"); check(re.search(r'배송 완료\s*<span class="count">\d+</span>', r.text) is not None, "상태 칩에 건수")

# 배송된 주문도 매출·리뷰 자격에 들어감
r = a.get(B + "/admin/members", params={"q": uid}); check("33,000원" in r.text, "배송 완료 주문도 구매액에 포함")

# 별점 입력
r = u.get(B + "/books/detail?id=ISBN1235")
check('class="star-field"' in r.text and r.text.count('class="star-input__radio"') == 5 and "5점</output>" in r.text, "별점 입력: 별 5개 + 점수 표시")
tu = csrf(r.text)
r = u.post(B + "/books/review", data={"_csrf": tu, "id": "ISBN1235", "rating": "3", "content": "보통이에요" + sfx})
check("리뷰를 남겼어요" in r.text and "별점 5점 중 3점" in r.text, "별점 3점 리뷰 저장·표시")
r = u.post(B + "/books/review", data={"_csrf": tu, "id": "ISBN1236", "rating": "9", "content": "x"}); check("구매한 회원만" in r.text or "1~5개" in r.text, "잘못된 별점 거절")

print(f"\n{ok}개 모두 통과")
