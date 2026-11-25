"""실제 소셜 로그인(OAuth 2.0) 흐름 테스트. 가짜 OAuth 서버(fake_oauth.py, 8097)와
그 서버를 가리키는 oauth.properties로 톰캣을 띄운 뒤 실행해요 (tests/README.md 참고)."""
import os, random, re, sys
import requests

B = os.environ.get("BASE_URL", "http://localhost:8080/BookMarketWeb")
F = os.environ.get("FAKE_OAUTH", "http://localhost:8097")
ok = 0
def check(cond, msg):
    global ok
    print(("PASS " if cond else "FAIL ") + msg)
    if not cond: sys.exit(1)
    ok += 1
def csrf(html): return re.search(r'name="_csrf" value="([^"]+)"', html).group(1)
def nxt(provider, **kw): requests.post(F + "/_next", json={"provider": provider, **kw})
def social(provider, path="/login", params=None):
    s = requests.Session()
    r = s.get(B + f"/oauth/{provider}{path}", params=params)
    return s, r

sfx = str(random.randint(10000, 99999))
kid = "31415" + sfx  # 10자리 숫자 ID (Double로 읽으면 3.14E9가 되는 크기)

r = requests.get(B + "/login"); check("카카오로 계속하기" in r.text and "테스트용 목업" not in r.text, "키가 있으면 로그인 화면에 목업 안내가 없음")

# 카카오: 처음 → 가입 + 추가 정보 화면
nxt("kakao", id=kid, name="카카오" + sfx, email=f"k{sfx}@kakao.test")
s, r = social("kakao")
check(r.url.endswith("/oauth/welcome") and "거의 다 됐어요" in r.text and f"k{sfx}@kakao.test" in r.text, "카카오 첫 로그인 → 가입 + 배송 정보 화면")
t = csrf(r.text)
r = s.post(B + "/oauth/welcome", data={"_csrf": t, "name": "카카오" + sfx, "phone": "12", "address": ""}); check("연락처는 숫자와" in r.text, "추가 정보: 잘못된 연락처 거절")
r = s.post(B + "/oauth/welcome", data={"_csrf": t, "name": "카카오" + sfx, "phone": "010-2222-" + sfx[:4], "address": "부산시 해운대구 1"})
check(r.url.endswith("/books") and "환영해요" in r.text, "추가 정보 저장 → 도서 목록")
r = s.get(B + "/mypage"); check("010-2222-" + sfx[:4] in r.text and "부산시 해운대구 1" in r.text, "마이페이지에 저장된 배송 정보")

# 다시 로그인 → 가입 화면 없이 로그인
s, r = social("kakao")
check(r.url.endswith("/books") and "카카오 계정으로 로그인했어요" in r.text, "두 번째부터는 바로 로그인")
a = requests.Session(); ta = csrf(a.get(B + "/login").text); a.post(B + "/login", data={"_csrf": ta, "loginId": "admin", "password": "admin1234"})
r = a.get(B + "/admin/members", params={"q": "카카오" + sfx}); check(r.text.count("/admin/members/detail?no=") == 2, "같은 카카오 계정은 회원 한 명")
no = re.search(r'/admin/members/detail\?no=(\d+)', r.text).group(1)
r = a.get(B + "/admin/members/detail", params={"no": no}); check("kakao_" + kid in r.text, "큰 숫자 카카오 ID도 그대로 저장 (" + kid + ")")

# next 유지
s, r = social("kakao", params={"next": "/cart"}); check(r.url.endswith("/cart"), "로그인 뒤 원래 가려던 화면으로")

# 네이버 (토큰 요청에 client_secret·state 필요)
nxt("naver", id="nv" + sfx + "AbC", name="네이버" + sfx, email=f"n{sfx}@naver.test")
s, r = social("naver"); check(r.url.endswith("/oauth/welcome") and "네이버" in r.text, "네이버 첫 로그인 → 가입")
r = s.get(B + "/books?skip=1"); check("네이버" + sfx in r.text or "nv" not in r.text, "나중에 입력하기 → 그대로 이용")
s, r = social("naver"); check("네이버 계정으로 로그인했어요" in r.text, "네이버 다시 로그인")

# 구글 (scope openid)
nxt("google", id="1098" + sfx + "77", name="Google " + sfx, email=f"g{sfx}@gmail.test")
s, r = social("google"); check(r.url.endswith("/oauth/welcome") and f"g{sfx}@gmail.test" in r.text, "구글 첫 로그인 → 가입")

# 이메일 없이도 가입 (카카오 이메일 동의 안 함)
nxt("kakao", id="27182" + sfx, name="", email="")
s, r = social("kakao"); check(r.url.endswith("/oauth/welcome") and 'value="카카오 회원"' in r.text, "이름·이메일 없으면 '카카오 회원'으로 가입")

# 취소 / 실패 / state 위조
nxt("kakao", id=kid, name="x", email="", deny=True)
s, r = social("kakao"); check(r.url.endswith("/login") and "카카오 로그인을 취소했어요" in r.text, "제공자 화면에서 취소")
nxt("google", id="1", name="x", email="", badcode=True)
s, r = social("google"); check(r.url.endswith("/login") and "정보를 받아 오지 못했어요" in r.text, "토큰 발급 실패 → 안내")
s = requests.Session(); s.get(B + "/login")
r = s.get(B + "/oauth/kakao/callback", params={"code": "x", "state": "forged"}); check("확인하지 못했어요" in r.text, "state가 다르면 거절 (CSRF 방지)")
r = requests.get(B + "/oauth/welcome"); check(r.url.endswith("/login"), "로그인 안 하고 추가 정보 화면 접근 → 로그인으로")

# 정지된 소셜 회원
ta = csrf(a.get(B + "/admin/members").text)
a.post(B + "/admin/members/status", data={"_csrf": ta, "no": no, "value": "SUSPENDED"})
nxt("kakao", id=kid, name="카카오" + sfx, email="")
s, r = social("kakao"); check(r.url.endswith("/login") and "정지된 계정이에요" in r.text, "정지된 소셜 회원 로그인 불가")
a.post(B + "/admin/members/status", data={"_csrf": ta, "no": no, "value": "ACTIVE"})

print(f"\n{ok}개 모두 통과")
