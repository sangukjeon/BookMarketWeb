"""관리자 회원 관리 테스트. e2e.py와 같은 조건(톰캣 실행 중)에서 돌려요. 회원을 새로 만들어 쓰므로 DB 초기화는 없어도 돼요."""
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
uid = "mem" + sfx

# 일반 회원 가입 + 주문 1건
u = requests.Session(); t = csrf(u.get(B + "/signup").text)
u.post(B + "/signup", data={"_csrf": t, "loginId": uid, "password": "pass1234", "password2": "pass1234", "name": "박회원" + sfx, "phone": "010-7777-" + sfx[:4], "address": "광주시 북구 용봉로 77"})
tu = csrf(u.get(B + "/mypage").text)
u.post(B + "/cart/add", data={"_csrf": tu, "id": "ISBN1235", "next": "/books"})
r = u.post(B + "/order", data={"_csrf": tu, "action": "place", "receiverName": "박회원", "receiverPhone": "010-7777-1111", "address": "광주시 북구 용봉로 77"})
check("/orders/receipt?id=" in r.url, "회원 가입 + 주문 1건")

# 접근 제어
r = u.get(B + "/admin/members"); check(r.status_code == 403, "일반 회원은 회원 관리 403")

# 관리자
a = requests.Session(); r = login(a, "admin", "admin1234")
check("회원 관리" in r.text and "오늘 가입" in r.text, "대시보드에 회원 요약 + 메뉴")
r = a.get(B + "/admin/members"); check(r.status_code == 200 and "박회원" + sfx in r.text and "관리자" in r.text, "회원 목록")
ta = csrf(r.text)
r = a.get(B + "/admin/members", params={"q": "박회원" + sfx}); check(r.text.count("/admin/members/detail?no=") == 2 and "1건" in r.text, "이름으로 찾기 + 주문 수")
no = re.search(r'/admin/members/detail\?no=(\d+)', r.text).group(1)
r = a.get(B + "/admin/members", params={"role": "ADMIN"}); check("박회원" + sfx not in r.text and ">관리자<" in r.text, "권한 필터: 관리자만")
r = a.get(B + "/admin/members", params={"provider": "KAKAO", "q": uid}); check("조건에 맞는 회원이 없어요" in r.text, "가입 방식 필터")
r = a.get(B + "/admin/members/detail", params={"no": no}); check("광주시 북구" in r.text and "주문 내역" in r.text and "정지하기" in r.text, "회원 상세: 정보 + 주문 + 관리 버튼")
r = a.get(B + "/admin/members/detail", params={"no": "999999"}); check(r.status_code == 404, "없는 회원 404")

# 자기 자신은 못 바꿈
r = a.get(B + "/admin/members", params={"q": "admin", "role": "ADMIN"}); me = re.search(r'/admin/members/detail\?no=(\d+)', r.text).group(1)
r = a.get(B + "/admin/members/detail", params={"no": me}); check("내 계정이에요" in r.text, "내 계정은 관리 버튼 없음")
r = a.post(B + "/admin/members/status", data={"_csrf": ta, "no": me, "value": "SUSPENDED"}); check("바꿀 수 없어요" in r.text, "내 계정 정지 막기")
r = a.post(B + "/admin/members/role", data={"_csrf": ta, "no": me, "value": "USER"}); check("바꿀 수 없어요" in r.text, "내 권한 내리기 막기")
r = a.get(B + "/admin"); check(r.status_code == 200, "관리자 권한 그대로")
r = a.post(B + "/admin/members/status", data={"no": no, "value": "SUSPENDED"}); check(r.status_code == 403, "CSRF 없으면 403")

# 정지 → 로그인해 있던 회원도 다음 요청에서 로그아웃
r = a.post(B + "/admin/members/status", data={"_csrf": ta, "no": no, "value": "SUSPENDED"}); check("계정을 정지했어요" in r.text and "정지됨" in r.text, "계정 정지")
r = u.get(B + "/mypage"); check(r.url.endswith("/login") and "로그아웃됐어요" in r.text, "로그인 중이던 회원 바로 로그아웃")
r = login(requests.Session(), uid, "pass1234"); check("정지된 계정이에요" in r.text, "정지된 계정은 로그인 불가")
r = login(requests.Session(), uid, "wrong999"); check("맞지 않아요" in r.text and "정지" not in r.text, "틀린 비밀번호면 정지 여부를 알리지 않음")
r = a.get(B + "/admin/members", params={"status": "SUSPENDED"}); check("박회원" + sfx in r.text, "상태 필터: 정지됨")
r = a.post(B + "/admin/members/status", data={"_csrf": ta, "no": no, "value": "ACTIVE"}); check("정지를 풀었어요" in r.text, "정지 풀기")
u = requests.Session(); r = login(u, uid, "pass1234"); check("반가워요" in r.text, "정지 푼 뒤 로그인")

# 권한: 로그인한 채로 바로 반영
r = a.post(B + "/admin/members/role", data={"_csrf": ta, "no": no, "value": "ADMIN"}); check("관리자로 바꿨어요" in r.text, "관리자로 올리기")
r = u.get(B + "/admin/members"); check(r.status_code == 200, "다시 로그인하지 않아도 관리자 화면 열림")
r = a.post(B + "/admin/members/role", data={"_csrf": ta, "no": no, "value": "USER"}); check("일반 회원으로 바꿨어요" in r.text, "일반 회원으로 내리기")
r = u.get(B + "/admin/members"); check(r.status_code == 403, "내린 뒤 바로 403")

# 비밀번호 초기화
r = a.post(B + "/admin/members/password", data={"_csrf": ta, "no": no}); m = re.search(r'class="num temp-pw">([a-z0-9]{10})<', r.text)
check(m is not None and "다시 볼 수 없어요" in r.text, "임시 비밀번호 한 번 보여 주기")
temp = m.group(1)
r = a.get(B + "/admin/members/detail", params={"no": no}); check(temp not in r.text, "새로 고치면 임시 비밀번호 사라짐")
r = login(requests.Session(), uid, "pass1234"); check("맞지 않아요" in r.text, "예전 비밀번호는 안 됨")
r = login(requests.Session(), uid, temp); check("반가워요" in r.text, "임시 비밀번호로 로그인")

# 소셜 회원은 비밀번호 초기화 없음
k = requests.Session(); r = k.get(B + "/oauth/kakao/login"); tk = csrf(r.text); st = re.search(r'name="state" value="([^"]+)"', r.text).group(1)
k.post(B + "/oauth/mock", data={"_csrf": tk, "provider": "kakao", "state": st, "name": "카톡" + sfx, "email": "k" + sfx + "@example.com", "action": "allow"})
r = a.get(B + "/admin/members", params={"q": "카톡" + sfx}); kno = re.search(r'/admin/members/detail\?no=(\d+)', r.text).group(1)
check("카카오" in r.text, "소셜 회원 목록에 가입 방식 표시")
r = a.get(B + "/admin/members/detail", params={"no": kno}); check("비밀번호가 없어요" in r.text and "초기화하기" not in r.text, "소셜 회원은 초기화 버튼 없음")
r = a.post(B + "/admin/members/password", data={"_csrf": ta, "no": kno}); check("비밀번호가 없어요" in r.text, "소셜 회원 초기화 요청 거절")
a.post(B + "/admin/members/status", data={"_csrf": ta, "no": kno, "value": "SUSPENDED"})
k2 = requests.Session(); r = k2.get(B + "/oauth/kakao/login"); tk = csrf(r.text); st = re.search(r'name="state" value="([^"]+)"', r.text).group(1)
r = k2.post(B + "/oauth/mock", data={"_csrf": tk, "provider": "kakao", "state": st, "name": "카톡" + sfx, "email": "k" + sfx + "@example.com", "action": "allow"})
check(r.url.endswith("/login") and "정지된 계정이에요" in r.text, "정지된 소셜 회원 로그인 불가")

print(f"\n{ok}개 모두 통과")
