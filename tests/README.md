# 자동 테스트

톰캣에서 BookMarketWeb이 실행 중일 때 HTTP로 전체 흐름을 검사해요. Python 3와 `requests`가 필요해요.

```
pip install requests
```

## 1. 기본 기능 (63개)
**DB를 처음 상태로 만들고, `api.properties`의 카카오 키를 비운 상태(DB 모드)**에서 실행하세요.

```
mysql -u root -p --default-character-set=utf8mb4 < ../sql/schema.sql
python e2e.py
```

## 2. 카카오 책 검색 (13개)
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
