<div align="center">

<img src="src/main/webapp/images/mascot/bear.png" alt="윤상문고 마스코트" width="140">

# 윤상문고 · BookMarketWeb

**교재의 Java Swing 프로젝트 `BookMarketSwing`을 Tomcat 위의 웹 서점으로 옮기고 기능을 보완한 팀 과제예요.**

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1-F8DC75?logo=apachetomcat&logoColor=black)
![JSP/Servlet](https://img.shields.io/badge/JSP%20%2F%20Servlet-MVC-2563EB)
![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Kakao](https://img.shields.io/badge/카카오-책%20검색%20API-FFCD00?logo=kakao&logoColor=black)

<img src="docs/screens/pc-books.png" alt="PC 도서 목록 화면" width="900">

</div>

## 한눈에 보기

| | |
|---|---|
| **구조** | MVC — Servlet(Controller) · JSP + JSTL(View) · DTO / DAO(Model) |
| **서버** | Apache Tomcat 10.1 (`jakarta.servlet`), Eclipse Dynamic Web Project |
| **데이터** | JDBC + 톰캣 DBCP 연결 풀. **Supabase(클라우드 PostgreSQL)** 또는 각자 PC의 MySQL — 같은 코드로 둘 다 동작 |
| **외부 API** | 카카오 책 검색 (알라딘 OpenAPI가 2026-11-01 종료돼서 교체) |
| **화면** | 자체 디자인 시스템 [`docs/DESIGN.md`](docs/DESIGN.md) — Pretendard, Lucide 아이콘, 라이트/다크, 모바일 + PC 레이아웃 |
| **원본 구조 유지** | `Item ← Book`, `Person ← Member`, `CartInterface ← Cart` 상속과 `Cart` 메서드 이름 |

---

## 실행 화면

### PC

| 도서 목록 · 장르 (마스코트) | 도서 검색 |
|---|---|
| <img src="docs/screens/pc-genre.png" alt="장르별 도서"> | <img src="docs/screens/pc-search.png" alt="검색"> |
| **도서 상세 · 재고** | **별점 리뷰** |
| <img src="docs/screens/pc-detail.png" alt="상세"> | <img src="docs/screens/pc-review-stars.png" alt="별점 리뷰"> |
| **장바구니 (DB 저장)** | **쿠폰함** |
| <img src="docs/screens/pc-cart.png" alt="장바구니"> | <img src="docs/screens/pc-coupons.png" alt="쿠폰함"> |
| **주문 내역** | **영수증 · 배송 진행** |
| <img src="docs/screens/pc-orders.png" alt="주문 내역"> | <img src="docs/screens/pc-receipt-shipping.png" alt="영수증"> |
| **로그인 (자체 + 소셜)** | **마이페이지 · 배송지 여러 개** |
| <img src="docs/screens/pc-login.png" alt="로그인"> | <img src="docs/screens/pc-mypage.png" alt="마이페이지"> |
| **관리자 · 매출 요약** | **관리자 · 주문 관리** |
| <img src="docs/screens/pc-admin.png" alt="관리자 대시보드"> | <img src="docs/screens/pc-admin-orders.png" alt="관리자 주문 관리"> |
| **관리자 · 도서(장르·재고)** | **관리자 · 쿠폰 (등록·사용 수)** |
| <img src="docs/screens/pc-admin-books.png" alt="관리자 도서"> | <img src="docs/screens/pc-admin-coupons.png" alt="관리자 쿠폰"> |
| **관리자 · 회원 관리** | |
| <img src="docs/screens/pc-admin-members.png" alt="관리자 회원 목록"> | |

### 모바일

| 장르 | 상세 · 재고 | 주문서 · 저장한 배송지 | 쿠폰함 | 영수증 | 메뉴 | 다크 모드 |
|---|---|---|---|---|---|---|
| <img src="docs/screens/m-genre.png" width="140"> | <img src="docs/screens/m-detail.png" width="140"> | <img src="docs/screens/m-order.png" width="140"> | <img src="docs/screens/m-coupons.png" width="140"> | <img src="docs/screens/m-receipt.png" width="140"> | <img src="docs/screens/m-menu.png" width="140"> | <img src="docs/screens/m-dark-books.png" width="140"> |

---

## 주요 기능

**도서**
- 카카오 책 검색(정확도순·최신순), 베스트셀러(윤상문고 판매량순)·신간 탭, 페이지 나누기
- **장르별 보기 (10개)**: 소설 · 시/에세이 · 인문 · 경제/경영 · 자기계발 · 과학 · IT/컴퓨터 · 어린이 · 만화 · 여행. 장르마다 권수, 많이 팔린 순/최신순, 상세에 "같은 장르의 다른 책". 카카오는 장르를 주지 않아서 장르 낱말로 찾은 책을 30분마다 가져와 그 장르로 저장(관리자가 고칠 수 있음)
- **재고 · 품절**: 책마다 재고(기본 20권), 3권 이하면 "N권 남음", 0이면 품절(구매 버튼 막힘). 주문하면 재고가 줄고(같은 트랜잭션에서 `stock >= 수량` 조건으로 차감, 모자라면 주문 전체 취소), 주문을 취소하면 돌아와요
- 검색으로 본 책은 `book` 테이블에 자동 저장 → 장바구니·주문·찜·리뷰에 그대로 사용
- 찜, 별점 리뷰(**구매한 회원만**, 1인 1개, 별 5개를 눌러 고르고 점수 표시), 최근 본 책 5권

**장바구니 · 주문**
- 수량 − / + (원본에서 **비어 있던 버튼을 구현**), 배송비 자동 계산 (3만 원 이상 무료)
- 장바구니 **DB 저장**: 로그인하면 저장된 장바구니와 합쳐져서 다른 PC에서도 그대로
- 주문서: 회원 정보·**저장한 배송지** 불러오기, **쿠폰함의 쿠폰 골라 쓰기(쿠폰당 1회)**, 결제 금액 요약
- 영수증 DB 저장·다시 보기·인쇄, **주문 취소** (취소하면 쿠폰 다시 사용 가능)
- **배송 단계**: 주문완료 → 배송 준비 중 → 배송 중 → 배송 완료. 영수증에 진행 막대, **배송이 시작되면 취소 불가**

**회원**
- 자체 회원가입·로그인 — **비밀번호 SHA-256 + salt 10,000회 암호화**
- **카카오·네이버·Google 실제 로그인 (OAuth 2.0)** — 처음이면 가입 + 배송 정보 입력, 다음부터는 바로 로그인. 키를 안 넣은 제공자는 테스트용 목업으로 동작
- 마이페이지: 내 정보, **배송지 최대 5개**(기본 배송지 지정), **비밀번호 바꾸기**, **회원 탈퇴**(개인정보는 지우고 주문 기록은 "탈퇴한 회원"으로 보관, 배송 중인 주문이 있으면 막음)
- **쿠폰함**: 쿠폰 번호를 등록해 두고 주문서에서 골라 써요. 가입하면 WELCOME10이 자동으로 들어 있어요
- 주문 내역, 찜 목록

**관리자** (`admin` / `admin1234`)
- 매출 요약: 오늘·전체 매출, 주문 수, 취소율, **최근 7일 매출 그래프**, 많이 팔린 책 TOP 5
- 주문 관리: 상태별 칩(건수), **한 단계씩 배송 진행**(배송 준비하기 → 배송 시작하기 → 배송 완료하기), 배송 전 주문만 취소
- 도서 등록·수정·삭제 (**장르 선택, 재고 입력**), 쿠폰 관리 (**쿠폰별 등록 수 / 사용 수**)
- **회원 관리**: 이름·아이디·이메일 검색, 가입 방식·권한·상태 필터, 회원별 주문 수·구매액, 회원 상세(정보 + 주문 내역)
  - **계정 정지 / 해제** — 정지하면 로그인할 수 없고, 이미 로그인해 있던 회원도 다음 요청에서 바로 로그아웃
  - **권한 변경** (일반 ↔ 관리자) — 다시 로그인하지 않아도 바로 반영. 내 계정은 바꿀 수 없음
  - **비밀번호 초기화** — 자체 가입 회원에게 임시 비밀번호를 만들어 한 번만 보여 줌

**보안**
- 모든 쿼리 `PreparedStatement`, 출력 `<c:out>` 이스케이프, 모든 POST에 CSRF 토큰
- 로그인 시 세션 ID 교체, 로그인·관리자 경로 접근 제어, 남의 영수증은 404
- Supabase 테이블은 RLS를 켜서 외부 REST API 접근 차단

---

## 구조

<img src="docs/architecture.png" alt="애플리케이션 구조도">

<details>
<summary><b>ERD (테이블 10개)</b></summary>

<img src="docs/erd.png" alt="ERD">

</details>

### 원본(Swing)에서 바뀐 점

| 원본 (Swing) | 웹 | 보완 내용 |
|---|---|---|
| GuestWindow (이름·연락처 입력) | `/login` `/signup` `/oauth/*` | 회원가입, **비밀번호 암호화**, 카카오·네이버·구글 로그인 |
| AdminLoginDialog (`Admin1234` 하드코딩) | DB 관리자 계정 | 코드에서 비밀번호 제거 |
| MainWindow 버튼 9개 | 상단 메뉴바 · 사이드바 / 폰 하단 시트 | 장바구니 개수 배지, 다크 모드 |
| CartAddItemPage | `/books` `/books/detail` | **카카오 책 검색 API**, 베스트셀러·신간, 찜, 리뷰, 최근 본 책 |
| **bt5 "항목수량 줄이기" (기능 없음)** | `/cart` | **수량 − / + 구현**, 1 아래로는 안 내려감 |
| CartShippingPage | `/order` | 회원 정보 불러오기, **쿠폰 적용**, 배송비 계산 |
| CartOrderBillPage | `/orders/receipt` `/orders` | 영수증 DB 저장, 다시 보기·인쇄, **주문 취소**, 배송 진행 막대 |
| AdminPage (도서 추가) | `/admin/*` | 매출 요약, 주문 관리, 도서·쿠폰·회원 관리 |
| BookInIt (`book.txt`) | `BookDAO` + `book` 테이블 | JDBC, 파일 끝 빈 줄로 책이 중복되던 버그 해결 |
| `Cart.mCartCount` (static) | 세션별 `Cart` | static이 모든 사용자에게 공유되는 문제 제거 |
| `Person.phone` (int) | `VARCHAR(20)` | `010`의 앞자리 0이 사라지는 문제 해결 |

---

## 빠르게 실행하기

**준비물:** JDK 17 이상 · Eclipse IDE for Enterprise Java and Web Developers · Apache Tomcat 10.1
(MySQL·PostgreSQL 드라이버와 JSTL jar는 `WEB-INF/lib`에 들어 있어요)

1. **받기** — Eclipse `File > Import > Git > Projects from Git > Clone URI`에 이 저장소 주소를 넣어요.
   (ZIP으로 받았다면 `File > Import > General > Existing Projects into Workspace`)
2. **설정 파일 만들기** — `src/main/java/`에서 두 파일을 복사해 이름을 바꿔요. 비밀번호·키가 들어가서 **GitHub에는 올라가지 않는 파일**이에요.
   - `db.properties.example` → `db.properties` (Supabase 또는 내 PC MySQL 중 하나)
   - `api.properties.example` → `api.properties` (카카오 REST API 키, 비워 두면 DB에 저장된 도서만 검색)
   - `oauth.properties.example` → `oauth.properties` (소셜 로그인 키, 비워 두면 목업 로그인 — 아래 "소셜 로그인 실제 연동")
3. **DB 만들기**
   - Supabase: SQL Editor에서 [`sql/schema-postgres.sql`](sql/schema-postgres.sql) 실행
   - MySQL: Workbench에서 [`sql/schema.sql`](sql/schema.sql) 실행
   - 이미 쓰던 DB라면 데이터를 지우지 않도록 추가용 SQL만 실행: [`migrate-member-status.sql`](sql/migrate-member-status.sql)(회원 관리) → [`migrate-shipping.sql`](sql/migrate-shipping.sql)(배송 단계) → `migrate-v3-postgres.sql` / `migrate-v3-mysql.sql`(장르·재고·배송지·쿠폰함·장바구니 저장)
4. **실행** — `Servers` 탭 → Tomcat v10.1에 `BookMarketWeb` 추가 → Start → http://localhost:8080/BookMarketWeb

| 테스트 계정 · 쿠폰 | |
|---|---|
| 관리자 | `admin` / `admin1234` |
| 일반 회원 | 회원가입 화면에서 직접 만들기 (영문+숫자 8자 이상) |
| `WELCOME10` | 10% 할인 (최대 5,000원) — 가입하면 쿠폰함에 자동으로 들어 있어요 |
| `BOOK3000` | 3만 원 이상이면 3,000원 할인 — 쿠폰함에서 번호를 등록해야 써요 |

<details>
<summary><b>Supabase(클라우드 DB) 설정 자세히</b> — 어느 PC에서든 같은 DB</summary>

1. https://supabase.com → **New project** → 지역 Northeast Asia, **Database Password**를 정하고 메모
2. **SQL Editor** → New query → `sql/schema-postgres.sql` 전체 붙여넣기 → **Run**
   → Table Editor에 테이블 10개(member, book, coupon, orders, order_item, wishlist, review, member_address, member_coupon, cart_item)가 보이면 성공
3. 위쪽 **Connect** → **Session pooler** 선택 → 연결 문자열에서 사용자·호스트 확인
   ```
   postgresql://postgres.abcdefgh:[YOUR-PASSWORD]@aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres
                └─ 사용자 ─────┘                 └─ 호스트 ──────────────────────────┘
   ```
4. `db.properties`
   ```properties
   db.driver=org.postgresql.Driver
   db.url=jdbc:postgresql://<호스트>:5432/postgres?sslmode=require
   db.user=postgres.<프로젝트ID>
   db.password=<DB 비밀번호 — 영문·숫자·기호만>
   db.pool.maxTotal=5
   ```
5. Eclipse 프로젝트 Refresh → `Project > Clean` → 톰캣 Clean → Restart

- **Direct connection 주소(`db.xxxx.supabase.co`)는 쓰지 않아요.** 무료 플랜에서는 IPv6 전용이라 보통 와이파이에서 연결되지 않아요.
- 처음 상태로 되돌리려면 `schema-postgres.sql`을 다시 실행해요.

</details>

<details>
<summary><b>내 PC MySQL 설정 자세히</b></summary>

1. MySQL Workbench에서 root로 접속 → `sql/schema.sql` 열기 → 전체 실행
2. `bookmarket` 스키마, 테이블 7개, 앱 계정 `bookmarket / bookmarket1234`가 만들어져요.
3. `db.properties`는 example의 **B. 내 PC MySQL** 부분을 그대로 쓰면 돼요.

명령 프롬프트로 하려면:
```
mysql -u root -p --default-character-set=utf8mb4 < sql\schema.sql
```

</details>

<details>
<summary><b>카카오 책 검색 키 넣기</b></summary>

1. https://developers.kakao.com → 내 애플리케이션 → 애플리케이션 추가
2. **앱 키 > REST API 키** 복사 → `api.properties`의 `kakao.restKey=` 뒤에 붙여 넣기
3. 서버 Restart → "자바" 검색 → 결과가 나오면 성공

- 검색은 카카오, 베스트셀러·신간 탭과 판매량순은 우리 DB예요 (카카오는 검색만 제공).
- 카카오에 연결하지 못하면 저장된 도서에서 찾고 안내 띠를 보여 줘요.

</details>

<details>
<summary><b>문제 해결</b></summary>

| 증상 | 해결 |
|---|---|
| `db.properties 파일을 찾지 못했어요` | `db.properties.example`을 복사해 `db.properties`로 만들었는지 확인 |
| `db.properties의 db.password에 실제 DB 비밀번호를 넣어 주세요` | `db.password=` 뒤의 자리 표시(`PUT_…`)를 실제 비밀번호로 바꾸고 Project Clean → Restart |
| `IllegalArgumentException: Prohibited character` | `db.password`에 한글·자리 표시(`여기에_…`)가 남아 있음 → 실제 비밀번호(영문·숫자·기호)로 바꾸고 Project Clean → Restart |
| 설정을 고쳤는데 그대로임 | Eclipse가 `build/classes`의 예전 복사본을 씀 → 프로젝트 Refresh(F5) → `Project > Clean` → 톰캣 Clean → Restart |
| `Unknown column 'status'` 등 | 최신 `schema.sql` / `schema-postgres.sql` 다시 실행 (데이터가 지워져요) |
| 회원 관리 화면에서 `column "status" does not exist` / `Unknown column 'status'` | 기존 데이터를 지키려면 [`sql/migrate-member-status.sql`](sql/migrate-member-status.sql) 한 줄만 실행 |
| 주문 화면에서 `column "shipped_at" does not exist` / `Unknown column 'shipped_at'` | [`sql/migrate-shipping.sql`](sql/migrate-shipping.sql) 두 줄 실행 |
| `column "stock" does not exist` / `relation "member_coupon" does not exist` / `Table 'bookmarket.cart_item' doesn't exist` | Supabase는 [`sql/migrate-v3-postgres.sql`](sql/migrate-v3-postgres.sql), MySQL은 [`sql/migrate-v3-mysql.sql`](sql/migrate-v3-mysql.sql) 실행 (기존 회원에게 WELCOME10도 넣어 줘요) |
| 별점 별이 안 보이거나 모양이 깨짐 | 예전 CSS가 캐시된 것 → 브라우저에서 Ctrl+F5 (강력 새로 고침) |
| `relation "book" does not exist` | Supabase SQL Editor에서 `schema-postgres.sql`을 실행했는지 확인 |
| Supabase `password authentication failed` | 사용자 이름이 `postgres.프로젝트ID` 통째인지, 비밀번호가 Database Password인지 확인 |
| Supabase 연결 시간 초과 | Session pooler 호스트·포트 5432인지 확인. 학교 와이파이가 막으면 휴대폰 테더링으로 확인 |
| Supabase 갑자기 접속 안 됨 | 대시보드에서 프로젝트가 Paused인지 확인 → Restore |
| `Access denied for user 'bookmarket'` | `schema.sql`을 root로 끝까지 실행했는지 확인 |
| `Public Key Retrieval is not allowed` | `db.url` 끝에 `&allowPublicKeyRetrieval=true&useSSL=false` 확인 |
| 한글이 `???`로 저장됨 | `schema.sql` 맨 위 `SET NAMES utf8mb4;`까지 포함해 실행 |
| 프로젝트에 빨간 X (jakarta 못 찾음) | 프로젝트 `Properties > Targeted Runtimes`에서 Tomcat 10.1 체크 |
| `KakaoBookClient cannot be resolved` | 프로젝트 Refresh → `Project > Clean` |
| 카카오 키를 넣었는데 안내 띠가 나옴 | Console의 `[KakaoBookClient] 응답 401` → REST API 키 오타·앞뒤 공백 확인 |

</details>

---

## 폴더 구조

```
src/main/java/com/market/
  controller/   Book · Cart · Auth · SocialLogin · Order · Orders · Wishlist · MyPage · Coupon · Admin Servlet
  api/          BookCatalog(카카오 ↔ DB 선택), KakaoBookClient, BookPage
  dao/          BookDAO, MemberDAO(탈퇴), OrderDAO(트랜잭션·재고·취소·매출), CouponDAO(쿠폰함), AddressDAO, CartDAO, WishlistDAO, ReviewDAO
  dto/          Item ← Book, Person ← Member, Order, OrderItem, Coupon, Review, Address, Genre(장르 enum)
  cart/         CartInterface ← Cart, CartItem (세션 저장)
  oauth/        SocialProvider ← OAuth2Provider(Kakao·Naver·Google) / MockSocialProvider, SocialProviders
  filter/       EncodingFilter, SecurityFilter(CSRF · 접근 제어)
  util/         DBUtil(연결 풀), PasswordUtil, MemberSessions(정지·권한 변경 즉시 반영), MiniJson, Web
src/main/webapp/
  css/          tokens · base · components · pc(1024px 이상)
  js/           theme(다크 모드) · ui(토스트 · 시트 · 확인 모달 · 배송지 칩)
  images/mascot 마스코트 곰 — 책 읽기(로고·목록), 인사(로그인), 만세(주문 완료), 돋보기(빈 화면), 시무룩(오류)
  WEB-INF/views JSP 화면 (common/ 공통 조각, admin/ 관리자)
sql/            schema.sql (MySQL), schema-postgres.sql (Supabase), migrate-*.sql (기존 DB에 칸·표만 추가)
tests/          HTTP 종단 테스트 205개 — 기본 68 + 회원 관리 33 + 배송·별점 24 + v3 기능 45 + 소셜 로그인 19 + 카카오 검색·장르 16
docs/           DESIGN.md, architecture.png, erd.png, screens/
```

## 테스트

DB 초기화 → 톰캣 실행 → `python tests/e2e.py` (68개) → `python tests/e2e_members.py` (회원 관리 33개) → `python tests/e2e_shipping.py` (배송·별점 24개) → `python tests/e2e_v3.py` (장르·재고·배송지·비밀번호·탈퇴·쿠폰함·장바구니 저장 45개). 카카오 검색·장르 가져오기는 가짜 서버로 `tests/e2e_kakao.py` (16개), 소셜 로그인은 가짜 OAuth 서버로 `tests/e2e_oauth.py` (19개). 자세한 방법은 [`tests/README.md`](tests/README.md).

<details>
<summary><b>소셜 로그인 실제 연동 (카카오·네이버·구글)</b></summary>

`oauth.properties`에 클라이언트 ID를 넣은 제공자는 **실제 OAuth 2.0**으로, 비워 둔 제공자는 목업으로 동작해요.
코드 구조는 `SocialProvider` 인터페이스 → `OAuth2Provider`(토큰·사용자 정보 받기 공통) → `Kakao/Naver/GoogleOAuthProvider`(주소와 JSON 모양만 다름)예요.

**모든 콘솔에 등록할 Redirect URI** (글자 하나라도 다르면 로그인이 안 돼요)
```
http://localhost:8080/BookMarketWeb/oauth/kakao/callback
http://localhost:8080/BookMarketWeb/oauth/naver/callback
http://localhost:8080/BookMarketWeb/oauth/google/callback
```

**카카오** — https://developers.kakao.com → 내 애플리케이션 (책 검색에 쓰는 앱 그대로 써도 돼요)
1. 제품 설정 → **카카오 로그인** → 활성화 설정 **ON**
2. 같은 화면의 **Redirect URI**에 위 카카오 주소 등록
3. 카카오 로그인 → **동의항목**: 닉네임 "필수 동의", (선택) 카카오계정 이메일
4. `kakao.clientId` = 앱 키의 **REST API 키**. (보안 → Client Secret을 "사용함"으로 켰다면 `kakao.clientSecret`도)

**네이버** — https://developers.naver.com → Application → 애플리케이션 등록
1. 사용 API: **네이버 로그인**, 제공 정보: 이름(또는 별명), 이메일
2. 환경: **PC 웹**, 서비스 URL `http://localhost:8080`, Callback URL에 위 네이버 주소
3. `naver.clientId`, `naver.clientSecret` = 발급된 Client ID / Client Secret
4. "개발 중" 상태에서는 **멤버관리 → 테스트 ID**에 등록한 네이버 아이디로만 로그인돼요 (팀원 아이디 추가)

**구글** — https://console.cloud.google.com
1. 새 프로젝트 → **API 및 서비스 → OAuth 동의 화면**: 외부(External), 앱 이름·이메일 입력, 범위 `openid` `email` `profile`, **테스트 사용자**에 로그인할 구글 계정 추가
2. **사용자 인증 정보 → OAuth 클라이언트 ID 만들기** → 웹 애플리케이션 → 승인된 리디렉션 URI에 위 구글 주소
3. `google.clientId`, `google.clientSecret` = 발급된 클라이언트 ID / 보안 비밀번호

넣은 뒤 Project Clean → Restart. 로그인 화면의 "목업" 안내가 사라지면 세 곳 모두 실제 연동된 거예요.

| 오류 | 원인 |
|---|---|
| 카카오 `KOE006` / 네이버 "redirect uri가 다릅니다" / 구글 `redirect_uri_mismatch` | 콘솔의 Redirect URI가 위 주소와 다름 (`127.0.0.1`과 `localhost`도 다른 주소로 봐요) |
| 카카오 `KOE004` / `KOE101` | 카카오 로그인이 꺼져 있거나 REST API 키가 아님 |
| 네이버 로그인 화면에서 막힘 | "개발 중" 앱인데 테스트 ID에 등록 안 된 아이디 |
| 구글 `access_denied` (403) | 동의 화면의 테스트 사용자에 그 계정이 없음 |
| "정보를 받아 오지 못했어요" | 시크릿 오타. Eclipse Console의 `[OAuth KAKAO] 토큰 응답 …` 줄에 이유가 나와요 |

</details>

---

<div align="center">
<sub>웹 프로그래밍 팀 과제 · 실제 결제는 일어나지 않는 연습용 서점이에요.</sub>
</div>
