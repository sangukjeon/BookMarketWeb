# 자동 테스트

톰캣에서 BookMarketWeb이 실행 중일 때 HTTP로 전체 흐름을 검사해요. Python 3와 `requests`가 필요해요.

```
pip install requests
```

## 1. 기본 기능 (68개)
**DB를 처음 상태로 만들고, `api.properties`의 카카오 키를 비운 상태(DB 모드)**에서 실행하세요.

```
mysql -u root -p --default-character-set=utf8mb4 < ../sql/schema.sql
python e2e.py
```

## 2. 회원 관리 (33개)
기본 테스트 다음에 바로 실행해도 되고, 회원을 새로 만들어 쓰므로 DB 초기화는 없어도 돼요.

```
python e2e_members.py
```
목록·검색·필터, 회원 상세, 계정 정지(로그인 중인 회원 즉시 로그아웃), 권한 변경 즉시 반영, 내 계정 보호, 비밀번호 초기화, 소셜 회원 처리를 확인해요.

## 3. 배송 단계 · 별점 (24개)
```
python e2e_shipping.py
```
관리자 배송 단계 진행(한 단계씩, 두 번 눌러도 한 번만), 배송 시작 뒤 취소 불가(회원·관리자), 영수증 진행 막대, 별점 입력·저장을 확인해요.

## 3-1. v3 기능 (45개)
```
python e2e_v3.py
```
장르별 목록·빈 장르 화면·같은 장르 책, 재고(관리자 수정·검사, 담기/늘리기 제한, 장바구니 경고, 주문 시 재고 차감·모자라면 거절, 취소하면 복구, 품절 버튼), 장바구니 DB 저장(다른 세션·비회원 장바구니 합치기), 배송지(최대 5개·기본·삭제·남의 것 보호·주문서 칩), 비밀번호 바꾸기, 탈퇴(진행 중 주문·비밀번호 확인·다른 세션 로그아웃·개인정보 지움·관리자 불가), 관리자 쿠폰 등록·사용 수를 확인해요. 바꾼 재고는 끝에 되돌려요.

## 4. 소셜 로그인 실제 연동 (19개)
가짜 OAuth 서버가 카카오·네이버·구글의 주소와 응답 모양을 흉내 내요. 톰캣의 `WEB-INF/classes/oauth.properties`가 이 서버를 가리켜야 해요.
```
python fake_oauth.py          # 포트 8097
```
`oauth.properties` (테스트용)
```
oauth.redirectBase=http://localhost:8080/BookMarketWeb
kakao.clientId=KAKAO_ID
kakao.authBase=http://localhost:8097/kakao
kakao.apiBase=http://localhost:8097/kakao
naver.clientId=NAVER_ID
naver.clientSecret=NAVER_SECRET
naver.authBase=http://localhost:8097/naver
naver.apiBase=http://localhost:8097/naver
google.clientId=GOOGLE_ID
google.clientSecret=GOOGLE_SECRET
google.authUrl=http://localhost:8097/google/auth
google.tokenUrl=http://localhost:8097/google/token
google.userUrl=http://localhost:8097/google/userinfo
```
```
python e2e_oauth.py
```
첫 로그인 → 가입 + 배송 정보 입력, 두 번째부터 바로 로그인, 큰 숫자 카카오 ID, 네이버 state·시크릿, 구글 scope, 취소·토큰 실패·state 위조, 정지 회원을 확인해요.
(키 없이 띄우면 목업 모드이고, 이때는 `e2e.py`가 목업 흐름을 확인해요)

## 5. 카카오 책 검색 · 장르 가져오기 (16개)
실제 카카오 대신 `fake_kakao.py`가 같은 형식으로 응답해요.

1. `python fake_kakao.py` (127.0.0.1:8098, 창을 열어 둔 채로)
2. 배포된 `WEB-INF/classes/api.properties`를 아래처럼 바꾸고 톰캣 재시작
   ```
   kakao.restKey=TESTKEY
   kakao.baseUrl=http://localhost:8098
   ```
3. DB 초기화 후 `python e2e_kakao.py`
   - DB 확인에 `mysql` 명령을 써요. Windows 예: `set MYSQL=C:\Program Files\MySQL\MySQL Server 26.10\bin\mysql.exe` 와 `set MYSQL_ARGS=-u root -p비밀번호`

끝나면 `api.properties`를 원래 키로 돌려 두세요.
