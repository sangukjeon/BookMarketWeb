# BookMarketWeb — 온라인 서점 (톰캣 + MySQL·Supabase)

교재의 Java Swing 프로젝트 **BookMarketSwing**을 **Tomcat 10.1(WAS) 위의 JSP/Servlet 웹 애플리케이션**으로 바꾸고, 기능을 보완한 팀 과제 프로젝트예요.

- 화면: `docs/DESIGN.md` 디자인 시스템 (Pretendard, Lucide 아이콘, 라이트/다크, 모바일 우선 + PC 2단)
- 데이터: JDBC + **Supabase(클라우드 PostgreSQL)** 또는 각자 PC의 MySQL (`book.txt` → `book` 테이블, 테이블 7개). `db.properties`로 골라요
- 외부 API: 카카오 책 검색 (알라딘 OpenAPI는 2026-11-01 서비스 종료로 교체)
- 구조: MVC — Servlet(Controller) / JSP(View) / DTO·DAO(Model)

| 문서 | 내용 |
|---|---|
| `docs/architecture.png` | 전체 애플리케이션 구조도 + 원본 Swing ↔ 웹 대응표 |
| `docs/erd.png` | ERD (테이블 7개) |
| `docs/screens/` | 실행 화면 (m- = 폰, pc- = PC) |
| `sql/schema.sql` | 테이블 생성 + 처음 데이터 (내 PC MySQL) |
| `sql/schema-postgres.sql` | 같은 구조의 Supabase(PostgreSQL)용 — SQL Editor에서 실행 |
| `sql/schema-cloud.sql` | (쓰지 않음) 예전 TiDB용 |
| `tests/` | 자동 테스트 (기본 63개 + 카카오 검색 13개) |
| `CLAUDE.md` | Claude Code가 읽는 작업 안내 (구조·규칙·다음 할 일) |

---

## 1. 준비물

| 항목 | 버전 |
|---|---|
| JDK | 17 이상 |
| Eclipse | Eclipse IDE for **Enterprise Java and Web** Developers |
| Apache Tomcat | **10.1** (jakarta.servlet) |
| MySQL | 8.x + MySQL Workbench |
| MySQL JDBC 드라이버 | `mysql-connector-j-8.x.x.jar` ← **직접 받아서 넣어야 해요** |

## 2. 실행 순서

### ① DB 만들기
1. MySQL Workbench에서 root로 접속
2. `sql/schema.sql` 열기 → 전체 실행(번개 아이콘)
3. `bookmarket` 스키마와 테이블 7개, 앱 전용 계정 `bookmarket / bookmarket1234`가 만들어져요.

명령 프롬프트로 하려면:
```
"C:\Program Files\MySQL\MySQL Server 26.10\bin\mysql.exe" -u root -p --default-character-set=utf8mb4 < "프로젝트경로\sql\schema.sql"
```

> 접속 정보는 `src/main/java/db.properties`에 있어요. 내 PC MySQL 기본값(`bookmarket / bookmarket1234`)이라 저장소에 함께 올라가요. 새로 받아도 따로 만들 필요가 없어요.
> `schema.sql`은 테이블을 지우고 새로 만들어요. 다시 실행하면 주문·회원 기록이 처음 상태로 돌아가요.

### ② JDBC 드라이버 넣기
1. https://dev.mysql.com/downloads/connector/j/ → **Platform Independent** → ZIP 받기
2. 압축을 풀고 `mysql-connector-j-8.x.x.jar`를 `src/main/webapp/WEB-INF/lib/`에 복사

`WEB-INF/lib`에는 JSTL(`taglibs-standard-*-migrated.jar`, Apache 라이선스)이 이미 들어 있어요.

### ③ (선택) 카카오 책 검색 키 넣기
키가 없어도 실행돼요. 이때는 북마켓 DB에 저장된 도서에서만 검색하고, 화면에 안내 띠가 나와요.
1. https://developers.kakao.com 로그인 → **내 애플리케이션 > 애플리케이션 추가하기** → 앱 만들기
2. **앱 키**의 **REST API 키** 복사
3. `src/main/java/api.properties.example`을 복사해 `api.properties`로 만들고 `kakao.restKey=` 뒤에 붙여 넣기 (이미 파일이 있으면 그 줄만 고치기)
4. 서버 Restart → 검색창에 "자바" 검색 → 아래에 "검색 결과 제공: 카카오"가 나오면 성공

- **검색**은 카카오(정확도순·최신순), **베스트셀러·신간 탭**과 **북마켓 판매량순**은 우리 DB예요. 카카오는 검색만 제공하거든요.
- 검색 결과로 본 책은 `book` 테이블에 자동 저장돼요(`source = KAKAO`). 장바구니·주문·찜·리뷰가 `book`을 외래키로 참조하기 때문이에요. 그래서 검색해 본 책이 신간 탭에도 쌓여요.
- 카카오에 연결하지 못하면 저장된 도서에서 찾고, 노란 안내 띠를 보여 줘요.
- `api.properties`는 키가 들어 있어서 **GitHub에 올리지 않아요**(`.gitignore`). 팀원은 각자 키를 넣거나, 키 없이 DB 모드로 써요.

### ④ 이클립스로 불러오기
1. `File > Import > General > Existing Projects into Workspace` → 이 폴더 선택
2. 톰캣 런타임 이름이 **`Apache Tomcat v10.1`** 이 아니면 빨간 오류가 나요.
   → 프로젝트 우클릭 `Properties > Targeted Runtimes`에서 자기 톰캣에 체크
3. 하단 `Servers` 탭 → 톰캣에 `BookMarketWeb` 추가 → Start
4. http://localhost:8080/BookMarketWeb 접속

### ⑤ Supabase(클라우드 PostgreSQL)로 바꾸기 — 어느 PC에서든 같은 DB
코드는 MySQL과 PostgreSQL 둘 다에서 동작해요. `db.properties`만 바꾸면 돼요.

1. https://supabase.com 가입 → **New project** → 지역 **Northeast Asia (Seoul)**, **Database Password**를 정하고 메모
2. 왼쪽 **SQL Editor** → **New query** → `sql/schema-postgres.sql` 내용을 전부 붙여넣고 **Run**
   - 왼쪽 **Table Editor**에 테이블 7개(member, book, coupon, orders, order_item, wishlist, review)가 보이면 성공
3. 위쪽 **Connect** 버튼 → **Session pooler** 선택 → 연결 문자열에서 세 값을 확인
   ```
   postgresql://postgres.abcdefgh:[YOUR-PASSWORD]@aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres
                └─ 사용자 ─────┘                 └─ 호스트 ──────────────────────────┘
   ```
4. `src/main/java/db.properties`의 **A** 부분을 채우고 `#`을 지운 뒤, **B** 네 줄 앞에 `#`을 붙여요
   ```
   db.driver=org.postgresql.Driver
   db.url=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
   db.user=postgres.abcdefgh
   db.password=정한 DB 비밀번호
   db.pool.maxTotal=5
   ```
5. `WEB-INF/lib`에 `postgresql-42.x.jar`가 있는지 확인 (MySQL jar는 같이 있어도 괜찮아요)
6. 이클립스 Refresh → 톰캣 Clean → Restart

- **다른 PC:** 같은 프로젝트에 `db.properties`만 똑같이 넣으면 같은 DB를 써요. 비밀번호가 들어 있으니 USB 등으로 직접 옮기세요.
- **Direct connection 주소(db.xxxx.supabase.co)는 쓰지 마세요.** 무료 플랜에서는 IPv6 전용이라 보통 와이파이에서는 연결되지 않아요. Session pooler는 IPv4로 무료 연결돼요.
- **보안:** `schema-postgres.sql`이 모든 테이블에 RLS를 켜 둬요. Supabase가 자동으로 여는 REST API로는 데이터를 읽거나 쓸 수 없고, 자바 앱(postgres 계정)만 쓸 수 있어요.
- **처음 상태로 되돌리기:** SQL Editor에서 `schema-postgres.sql`을 다시 실행하면 돼요.
- MySQL로 돌아가려면 A 앞에 `#`, B의 `#`을 지우고 Restart.

## 3. 테스트 계정

| 구분 | 아이디 | 비밀번호 |
|---|---|---|
| 관리자 | `admin` | `admin1234` |
| 일반 회원 | 회원가입 화면에서 직접 만들기 | 영문+숫자 8자 이상 |
| 구글/네이버 | 로그인 화면의 버튼 → 목업 화면에서 이름·이메일 입력 | — |

| 쿠폰 | 혜택 |
|---|---|
| `WELCOME10` | 10% 할인 (최대 5,000원) |
| `BOOK3000` | 3만 원 이상이면 3,000원 할인 |

- 쿠폰은 **회원마다 한 번**씩 쓸 수 있어요. 그 주문을 취소하면 다시 쓸 수 있어요.
- 배송비: 할인 뒤 금액이 3만 원 미만이면 3,000원, 이상이면 무료.

## 3-1. 기능 한눈에 보기

| 화면 | 기능 |
|---|---|
| 도서 `/books` | 베스트셀러(북마켓 판매량 순)·신간(출판일 순) 탭, 카카오 책 검색(정확도·최신, 키 없으면 DB 검색), 북마켓 판매량순, 페이지, 최근 본 책 5권 |
| 상세 `/books/detail` | 찜(하트), 별점 평균, 리뷰(**구매한 회원만**, 1인 1개, 이름 가림), 담기·바로 구매 |
| 장바구니 `/cart` | 수량 − / +, 빼기, 비우기, 배송비 안내 |
| 주문서 `/order` | 회원 정보로 채우기, 쿠폰(1회), 결제 금액 요약 |
| 영수증 `/orders/receipt` | 인쇄, **주문 취소**(확인 창 → 상태 "취소됨", 기록 유지) |
| 주문 내역 · 찜 · 내 정보 | 취소 표시, 찜 목록에서 바로 담기, 기본 배송지 저장 |
| 로그인 | 카카오·네이버·Google(목업), 아이디 로그인, 회원가입 |
| 관리자 `/admin` | 매출 요약(오늘·전체 매출, 주문 수, 취소율, TOP 5), 주문 관리(상태 필터·취소), 도서 관리, 쿠폰 관리 |

## 4. 원본에서 바뀐 점 (발표용 요약)

| 원본 (Swing) | 웹 | 보완 내용 |
|---|---|---|
| GuestWindow (이름·연락처 입력) | `/login` `/signup` `/oauth/*` | 회원가입, **비밀번호 SHA-256 + salt 암호화**, 구글/네이버 소셜 로그인(목업) |
| AdminLoginDialog (`Admin1234` 하드코딩) | DB 관리자 계정 | 코드에서 비밀번호 제거 |
| MainWindow 버튼 9개 | 상단 메뉴바 / 폰 하단 시트 메뉴 | 장바구니 개수 배지, 다크 모드 |
| CartAddItemPage | `/books` `/books/detail` | **카카오 책 검색 API**, 베스트셀러·신간, 찜, 리뷰, 최근 본 책 |
| **bt5 "항목수량 줄이기" (기능 없음)** | `/cart` | **수량 − / + 구현**, 1 아래로는 안 내려감 |
| CartShippingPage | `/order` | "회원 정보와 같아요" 선택, **쿠폰 적용**, 배송비 계산 |
| CartOrderBillPage | `/orders/receipt` `/orders` | 영수증을 DB에 저장, 다시 보기·인쇄, **주문 취소** |
| AdminPage (도서 추가) | `/admin` `/admin/orders` `/admin/books` `/admin/coupons` | 매출 요약, 주문 관리, 도서 등록·수정·삭제, 쿠폰 관리 |
| BookInIt (`book.txt`) | `BookDAO` + `book` 테이블 | JDBC, 파일 끝 빈 줄 때문에 책이 중복으로 읽히던 버그 해결 |
| `Cart.mCartCount` (static) | 세션별 `Cart` | 웹에서는 static이 모든 사용자에게 공유되므로 제거 |
| `Person.phone` (int) | `VARCHAR(20)` | `010`의 앞자리 0이 사라지는 문제 해결 |

원본의 상속 구조(`Item ← Book`, `Person ← Member`, `CartInterface ← Cart`)는 그대로 살렸어요.

## 5. 보안

- 비밀번호: 회원마다 다른 salt + SHA-256 10,000번 반복 (`PasswordUtil`)
- SQL Injection: 모든 쿼리에 `PreparedStatement`
- XSS: 사용자 입력은 JSP에서 `<c:out>`으로 출력
- CSRF: 모든 POST에 세션별 `_csrf` 토큰 (`SecurityFilter`)
- 세션 고정: 로그인할 때 `changeSessionId()`
- 접근 제어: 주문·내 정보는 로그인 필요, `/admin`은 관리자만, 남의 영수증은 404
- 소셜 로그인: `state` 값으로 위조 요청 차단, `code`는 한 번만 사용
- 주문 취소·리뷰 삭제: 본인 것만(관리자는 전체), 이미 취소된 주문은 다시 취소 안 됨

## 6. 소셜 로그인을 실제로 바꾸려면

지금은 `MockSocialProvider`가 구글/네이버 서버 역할을 해요. 흐름(state → code → 사용자 정보)은 실제 OAuth 2.0과 같아서, 아래만 하면 돼요.

1. 키 발급
   - 구글: Google Cloud Console → OAuth 클라이언트 ID (웹 애플리케이션)
   - 네이버: NAVER Developers → 애플리케이션 등록 (네아로)
   - 리디렉션 URI: `http://localhost:8080/BookMarketWeb/oauth/google/callback` (네이버는 `/naver/callback`)
2. `com.market.oauth.SocialProvider`를 구현한 `GoogleOAuthProvider`, `NaverOAuthProvider` 작성
   - `authorizeUrl()`: 각 회사 인증 주소 + client_id + redirect_uri + state
   - `fetchProfile()`: code로 토큰 요청 → 토큰으로 사용자 정보 요청 (JDK `HttpClient`)
3. `SocialProviders`의 `Map.of(...)`에서 Mock 대신 새 클래스로 교체
4. 공식 버튼 가이드에 맞게 로그인 화면 버튼 교체 (DESIGN.md 부록 참고)

컨트롤러(`SocialLoginServlet`)와 DB는 고칠 필요가 없어요.

## 7. 폴더 구조

```
src/main/java/
  db.properties                 DB 접속 정보 (GitHub에 올리지 않음)
  db.properties.example         접속 정보 예시 (내 PC MySQL / TiDB)
  api.properties                카카오 REST API 키 (GitHub에 올리지 않음, 예시는 api.properties.example)
  com/market/
    controller/  Book·Cart·Auth·SocialLogin·Order·Orders·Wishlist·MyPage·Admin Servlet
    api/         BookCatalog(카카오↔DB 선택), KakaoBookClient, BookPage
    dao/         BookDAO, MemberDAO, OrderDAO(트랜잭션·취소·매출), CouponDAO, WishlistDAO, ReviewDAO, SalesSummary
    dto/         Item, Book, Person, Member, Order, OrderItem, Coupon, Review
    cart/        CartInterface, Cart, CartItem (세션 저장)
    oauth/       SocialProvider, MockSocialProvider, SocialProviders, SocialProfile
    filter/      EncodingFilter, SecurityFilter
    util/        DBUtil(연결 풀), DBShutdownListener, PasswordUtil, MiniJson(JSON 파서), Web
src/main/webapp/
  css/  tokens.css(DESIGN.md 11장 그대로) · base.css · components.css
  js/   theme.js(다크 모드) · ui.js(토스트·시트·확인 모달)
  images/books/  표지, images/icons.svg (Lucide, ISC 라이선스)
  WEB-INF/views/  JSP 화면, WEB-INF/tags/ 아이콘·분야 태그
sql/schema.sql, sql/schema-cloud.sql
docs/  DESIGN.md, architecture.png, erd.png, screens/
```

## 8. 문제 해결

| 증상 | 해결 |
|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | ② JDBC 드라이버를 `WEB-INF/lib`에 넣었는지 확인 |
| `Access denied for user 'bookmarket'` | `schema.sql`을 root로 끝까지 실행했는지, `db.properties` 비밀번호 확인 |
| 한글이 `???`로 저장됨 | `schema.sql` 맨 위 `SET NAMES utf8mb4;`까지 포함해 실행 |
| 프로젝트에 빨간 X (jakarta 못 찾음) | ④-2 Targeted Runtimes에서 Tomcat 10.1 체크 |
| 숫자에 쉼표가 안 나옴 | `web.xml`의 `fmt.locale` 설정이 지워지지 않았는지 확인 |
| `Public Key Retrieval is not allowed` | `db.properties`의 `db.url` 끝에 `&allowPublicKeyRetrieval=true&useSSL=false`가 있는지 확인 |
| 카카오 키를 넣었는데 노란 띠가 나옴 | Console의 `[KakaoBookClient] 응답 401` → 키 오타(REST API 키가 맞는지, 앞뒤 공백), 그 밖의 코드는 인터넷 연결 확인 |
| `Unknown column 'status'` 등 | 새 버전 `schema.sql`을 다시 실행 |
| `db.properties 파일을 찾지 못했어요` | `db.properties.example`을 `db.properties`로 복사했는지 확인 (GitHub에는 올라가지 않는 파일이에요) |
| Supabase: `Connection attempt failed` / 연결 시간 초과 | Session pooler 호스트(`…pooler.supabase.com`)·포트 5432인지 확인. Direct 주소(`db.xxxx.supabase.co`)는 IPv6라 안 돼요. 학교 와이파이가 5432를 막으면 휴대폰 테더링으로 확인 |
| Supabase: `password authentication failed` | 사용자 이름이 `postgres.프로젝트ID` 통째인지, 비밀번호가 Database Password인지 확인 (Settings → Database에서 재설정 가능) |
| Supabase: `relation "book" does not exist` | SQL Editor에서 `schema-postgres.sql`을 실행했는지 확인 |
| Supabase: 갑자기 접속이 안 됨 | 대시보드에서 프로젝트가 일시 정지(Paused)됐는지 확인 → Restore |
| `ClassNotFoundException: org.postgresql.Driver` | `WEB-INF/lib`에 `postgresql-42.x.jar`가 있는지 확인 |
| TiDB: `Connections using insecure transport are prohibited` | `db.url`에 `sslMode=VERIFY_IDENTITY`가 있는지, `useSSL=false`가 남아 있지 않은지 확인 |
| TiDB: `Access denied for user` | 사용자 이름은 앞자리까지 통째로(`xxxx.root`), 비밀번호는 Generate Password로 새로 만든 값인지 확인 |
| TiDB: `Communications link failure` / 연결 시간 초과 | 포트가 4000인지, 학교·회사 와이파이가 4000번 포트를 막지 않는지 확인 (휴대폰 테더링으로 시험) |
| TiDB: `Unknown database 'bookmarket'` | `schema-cloud.sql`을 끝까지 실행했는지 확인 |
| TiDB: 갑자기 접속이 안 됨 | 이번 달 무료 한도를 다 썼을 수 있어요. 콘솔의 Usage를 확인 (다음 달 1일에 초기화) |
| TiDB: 주문 번호가 1, 2, 30001처럼 건너뜀 | TiDB의 AUTO_INCREMENT 특징이에요. 기능에는 문제없어요 |

## 9. Claude Code로 이어서 작업하기
1. 이 폴더(`BookMarketWeb`)에서 터미널을 열고 `claude` 실행
2. Claude Code가 `CLAUDE.md`를 먼저 읽어서 구조·규칙·다음 할 일을 알고 시작해요
3. 예: "CLAUDE.md의 다음 할 일 1번(회원 쿠폰함)부터 진행해줘"
