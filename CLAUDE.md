# CLAUDE.md — BookMarketWeb 작업 안내

웹 프로그래밍 팀 과제. 교재의 Java Swing 프로젝트 **BookMarketSwing**을 **Tomcat 10.1 + JSP/Servlet(MVC) + MySQL(JDBC)** 웹 서점으로 바꾼 프로젝트다.
사람이 보는 설명은 `README.md`, 화면 규칙은 `docs/DESIGN.md`에 있다.

## 협업 규칙
- 답변과 화면 문구는 **한국어**. 화면 문구는 "~요" 정중체, 버튼은 동사로 시작하고 마침표 없이 (`주문 취소하기`).
- 새 기능이나 큰 변경은 **① 이해한 내용 요약 → ② 실행 계획 → ③ 사용자 최종 확인** 뒤에 진행한다.
- 기능을 바꾸면 `tests/`의 테스트를 고치거나 추가하고, 끝나면 `README.md`(기능·문제 해결)와 `docs/erd.png`(테이블이 바뀐 경우)를 갱신한다.
- 이 프로젝트는 원본 Swing 구조를 살린 것이 발표 포인트다. `Item ← Book`, `Person ← Member`, `CartInterface ← Cart`와 `Cart`의 메서드 이름(`insertBook`, `isCartInBook`, `removeCart`, `deleteBook`)은 바꾸지 않는다.

## 기술 스택
- JDK 17 (`--release 17`), Tomcat **10.1** (`jakarta.servlet.*`), 서블릿 6.0
- JSP + JSTL: `WEB-INF/lib/taglibs-standard-*-migrated-0.0.1.jar` (Apache Taglibs를 jakarta로 옮긴 것)
- DB: **Supabase(PostgreSQL)** 또는 각자 PC의 MySQL 8 — `db.properties`로 고른다. 드라이버 `WEB-INF/lib`: `postgresql-42.x.jar`, `mysql-connector-j-*.jar`. TiDB는 쓰지 않는다
- **SQL은 MySQL·PostgreSQL 둘 다에서 돌아가게 쓴다**: 자동 번호는 `prepareStatement(sql, new String[]{"컬럼"})`, 불리언은 `TRUE/FALSE`·`NOT active`(정수 1/0 금지), 대소문자 무시 검색은 `LOWER(col) LIKE ?`, GROUP BY에 SELECT한 다른 테이블 컬럼도 넣기, 제약 위반은 `DBErrors.isConstraintViolation`(SQLSTATE 23xxx). 백틱·`AUTO_INCREMENT`·`ON DUPLICATE KEY`·`LIMIT a,b` 금지(`LIMIT ? OFFSET ?`는 됨)
- 테이블을 바꾸면 `sql/schema.sql`(MySQL)과 `sql/schema-postgres.sql`(Supabase) **둘 다** 고친다
- 연결 풀: 톰캣 내장 DBCP(`org.apache.tomcat.dbcp.dbcp2.BasicDataSource`, `<톰캣>/lib/tomcat-dbcp.jar`). 외부 jar 아님
- 빌드 도구 없음(Maven/Gradle 없음). 이클립스 Dynamic Web Project 구조. 외부 라이브러리를 늘리지 않는 것이 원칙 (JSON도 `util/MiniJson`으로 직접 파싱)

## 폴더
```
src/main/java/
  db.properties          DB 접속 정보 (DBUtil이 클래스패스에서 읽음). 비밀번호가 들어가므로 **.gitignore 대상** (example을 복사해서 만든다)
  db.properties.example  접속 예시: A. Supabase / B. 내 PC MySQL
  api.properties         카카오 REST API 키 (비어 있으면 DB 모드). .gitignore 대상, 예시는 api.properties.example
  com/market/
    controller/  서블릿 (@WebServlet). 요청 → DAO → request 속성 → Web.render("뷰이름")
    api/         BookCatalog(검색은 카카오, 탭은 DB), KakaoBookClient, BookPage. AladinClient.java는 알라딘 서비스 종료(2026-11-01)로 쓰지 않음 — 지워도 됨
    dao/         JDBC. 모든 SQL은 PreparedStatement. OrderDAO.insert는 트랜잭션
    dto/         Item, Book, Person, Member, Order, OrderItem, Coupon, Review
    cart/        세션 장바구니 (원본 유지)
    oauth/       SocialProvider 인터페이스 + MockSocialProvider (구글·네이버·카카오 목업)
    filter/      EncodingFilter, SecurityFilter (web.xml에 순서대로 등록, @WebFilter 쓰지 않음)
    util/        DBUtil(DBCP 연결 풀, getConnection()은 풀에서 빌림), DBShutdownListener(web.xml 등록, 종료 시 풀 닫기),
                 PasswordUtil(SHA-256+salt 10,000회, tempPassword), MemberSessions(관리자가 바꾼 회원의 세션을 다음 요청에서 DB로 다시 읽게 함), MiniJson, Web(render·redirect·flash·login)
src/main/webapp/
  WEB-INF/views/   JSP. 공통 조각은 common/*.jspf (taglibs, header, footer, csrf, cover, stars)
  WEB-INF/tags/    icon.tag(Lucide 스프라이트), cat.tag(분야 태그), brand.tag(소셜 로고)
  WEB-INF/web.xml  필터 순서, DB 종료 리스너, fmt 로케일(ko_KR), 오류 페이지, *.jspf UTF-8
  css/tokens.css   docs/DESIGN.md 11장과 값이 같아야 함 (직접 고치지 말고 DESIGN.md와 함께)
  css/components.css  컴포넌트 구현 (모바일 기본)
  css/pc.css          PC(1024px~) 전용 레이아웃: 상단 바 검색, 사이드바(.with-side/.side), 세로형 도서 카드 4열, 상세 구매 카드(.buybox), 장바구니 표, 관리자 1440 폭·7일 매출 그래프. 모바일은 건드리지 않는다. 화면별 표시는 .pc-only / .m-only
  js/              theme.js(다크 모드), ui.js(토스트·시트·확인 모달·펼침)
  images/icons.svg Lucide 아이콘 스프라이트
sql/schema.sql     DB 전체 생성 (DROP 후 다시 만듦 → 실행하면 데이터 초기화)
sql/schema-postgres.sql  Supabase(PostgreSQL)용. SQL Editor에서 실행. RLS를 켜서 REST API 접근을 막음
sql/migrate-member-status.sql, migrate-shipping.sql  기존 DB에 컬럼만 추가 (데이터 유지)
tests/             HTTP 종단 테스트 (tests/README.md)
docs/              DESIGN.md, erd.png, architecture.png, screens/, src/(다이어그램 HTML 원본)
```

## 실행
- **이클립스(사용자 기본):** Servers 탭 → Tomcat v10.1 → Add and Remove로 BookMarketWeb 추가 → Start → http://localhost:8080/BookMarketWeb
- **DB 초기화:** `mysql -u root -p --default-character-set=utf8mb4 < sql/schema.sql` (테이블 7개 + 앱 계정 `bookmarket/bookmarket1234` + 관리자 `admin/admin1234` + 쿠폰 2개)
- **Supabase:** SQL Editor에서 `sql/schema-postgres.sql` 실행 → `db.properties`를 example의 A로. 접속 정보·비밀번호는 사용자가 직접 넣는다 (채팅으로 받지 않는다)
- **이클립스 없이 컴파일 확인:**
  ```
  javac --release 17 -encoding UTF-8 -cp "<톰캣>/lib/servlet-api.jar;<톰캣>/lib/tomcat-dbcp.jar" -d build/classes $(find src/main/java -name "*.java")
  ```
  (macOS/Linux는 클래스패스 구분자가 `;` 대신 `:`)
  배포는 `src/main/webapp` 전체 + `build/classes` + `db.properties`, `api.properties`를 `<톰캣>/webapps/BookMarketWeb/`(`WEB-INF/classes`)에 복사.
- **테스트:** DB 초기화 → 톰캣 실행 → `python tests/e2e.py` (63개) → `python tests/e2e_members.py` (회원 관리 33개) → `python tests/e2e_shipping.py` (배송·별점 24개). 카카오 검색은 `tests/fake_kakao.py` + `tests/e2e_kakao.py` (13개, tests/README.md).

## 꼭 지킬 것 (실제로 겪은 함정)
- **JSTL URI**는 `http://java.sun.com/jsp/jstl/core` 형식. `jakarta.tags.core`는 이 jar에서 동작하지 않는다.
- EL에서 `empty`는 예약어 → `cart.empty` 대신 `cart.isEmpty()`.
- **모든 POST 폼**에 `<%@ include file="/WEB-INF/views/common/csrf.jspf" %>` (없으면 403).
- 사용자 입력 출력은 `<c:out>` 또는 `fn:escapeXml`. 속성값 안에서는 `value="<c:out value='${x}'/>"`.
- 숫자 표시는 `<fmt:formatNumber value="${n}" pattern="#,##0" />` (web.xml 로케일 설정 덕분에 쉼표가 찍힘).
- 시간은 DB 기본값 대신 Java에서 `LocalDateTime.now(ZoneId.of("Asia/Seoul"))`로 넣는다 (DB 서버 시간대가 달라도 맞게).
- 로그인 필요 경로는 `SecurityFilter`의 `needLogin`에 추가. 관리자 전용은 `/admin` 아래에 둔다.
- 폼 뒤 돌아갈 곳은 hidden `next` + `Web.safeNext()` (외부 주소로 보내지 않게).
- 아이콘 추가: Lucide SVG의 내용을 `images/icons.svg`에 `<symbol id="i-이름" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">`로 넣는다. **symbol에 `fill="none"`을 넣지 않는다** (CSS `.icon{fill:none}`이 기본값이고, 채운 하트·별은 CSS로 `fill: currentColor`).
- 이모지 금지, 아이콘은 Lucide만. 파란 채움 버튼(`.btn-main`)은 한 화면에 하나. 비활성은 투명도 대신 색 교체.
- 되돌릴 수 없는 POST(삭제·취소)는 폼에 `data-confirm="질문"` → ui.js가 확인 모달을 띄운다.
- **EL 두 개를 공백으로 이어 붙이면 공백이 사라질 수 있다** (`class="${a ? 'x' : ''} ${b ? 'y' : ''}"` → `xy`). 공백은 따옴표 안에 넣는다: `${a ? 'x' : ''}${b ? ' y' : ''}`. 태그 사이 공백만 있는 텍스트도 마찬가지 (`<span> 외 1종</span>`처럼 안에 넣기).
- 별점 입력은 `fieldset.star-field` 안의 `div.star-input`에서 flex를 건다 (fieldset 자체에 flex를 걸면 브라우저마다 다르게 그림).

## 데이터 규칙
- `book.source`: `LOCAL`(관리자 등록) / `KAKAO`(검색 결과로 보일 때 자동 저장, 예전 `ALADIN` 행이 남아 있을 수 있음). API 갱신은 `LOCAL`이 아닌 행만 덮어쓴다. 카카오는 분야(category)를 주지 않는다.
- `orders.status`: `ORDERED`(주문완료) → `PREPARING`(배송 준비 중) → `SHIPPING`(배송 중, `shipped_at`) → `DELIVERED`(배송 완료, `delivered_at`), 그리고 `CANCELLED`.
  - 관리자만 한 단계씩 앞으로 (`OrderDAO.advance(id, from)` = `UPDATE … WHERE status = from`, 두 번 눌러도 한 번만). 순서·이름은 `Order.FLOW`/`labelOf`/`getStatusPill` 한 곳에서 관리.
  - 취소는 `ORDERED`·`PREPARING`에서만 (회원·관리자 모두, `Order.isCancellable`).
  - 매출·판매량·리뷰 자격·쿠폰 사용 여부는 **`status <> 'CANCELLED'`** 로 센다 (`= 'ORDERED'`로 쓰지 않는다).
- 쿠폰: 회원당 1회 (`OrderDAO.hasUsedCoupon`), 그 주문을 취소하면 다시 쓸 수 있다.
- 리뷰: 그 책을 산(취소 안 된) 회원만, 책 1권에 1개 (`uk_review_once`).
- `member.status`: `ACTIVE` / `SUSPENDED`. 정지 회원은 자체·소셜 로그인 모두 막고(비밀번호가 맞을 때만 "정지" 안내), 로그인 중이면 `SecurityFilter`가 다음 요청에서 로그아웃시킨다. 관리자가 회원 상태·권한을 바꾸면 반드시 `MemberSessions.changed(memberNo)`를 부른다.
- 회원 관리(`/admin/members`): 관리자는 **자기 계정의 상태·권한을 바꿀 수 없다**(관리자가 0명이 되는 일 방지). 회원 삭제는 하지 않는다(주문·리뷰 FK). 비밀번호 초기화는 `provider = 'LOCAL'`만, 임시 비밀번호는 세션에 잠깐 두고 한 번만 보여 준다.
- 기존 DB에 컬럼 추가는 `sql/migrate-*.sql`로 따로 둔다 (schema 재실행은 데이터 초기화).

## 다음 할 일 (사용자와 합의 완료, 아직 구현 전)
1. **회원 쿠폰함 (쿠폰 직접 등록)**
   - 새 테이블 `member_coupon(member_no, code, registered_at)` PK(member_no, code), FK 둘 다.
   - `/coupons` (로그인 필요): 쿠폰 번호 입력 → 등록, 내 쿠폰 목록(상태: 사용 가능 / 사용함 / 멈춤).
   - 주문서 쿠폰은 **내 쿠폰함에 등록한 것만** 선택 가능 (직접 입력 칸 → 내 쿠폰 선택으로 변경).
   - 회원가입(자체·소셜 첫 로그인) 때 `WELCOME10` 자동 등록. 관리자 쿠폰 화면에 "등록 회원 수 / 사용 횟수".
2. ~~배송 단계와 취소 제한~~ **완료 (2026-11-18)** — 아래는 당시 계획 기록
   - `orders.status`: `ORDERED`(주문완료) → `PREPARING`(배송 준비 중) → `SHIPPING`(배송 중) → `DELIVERED`(배송 완료), 그리고 `CANCELLED`. 컬럼 `shipped_at`, `delivered_at` 추가.
   - 관리자 주문 관리에서 한 단계씩 앞으로만 변경 (UPDATE … WHERE status = 이전 상태).
   - **취소는 ORDERED·PREPARING에서만** (회원·관리자 모두). `OrderDAO.cancel`의 WHERE를 `status IN ('ORDERED','PREPARING')`로. 화면에는 배송 진행 막대와 "배송이 시작돼서 취소할 수 없어요" 안내.
   - 매출·판매량·쿠폰·리뷰 판단의 "취소되지 않은 주문"은 `status <> 'CANCELLED'`로 바꿔야 한다 (지금 `= 'ORDERED'`로 쓴 곳 모두: BookDAO.SOLD, OrderDAO.hasUsedCoupon/hasPurchased/summary).
3. ~~클라우드 DB (TiDB Cloud Starter)~~ **사용 중단 → Supabase(PostgreSQL)로 대체 (2026-11-18)**. 관련 파일(schema-cloud.sql, example의 B)은 참고용으로만 남김. 기존 기록:: `schema-cloud.sql`, `db.properties.example` + `.gitignore`, DBUtil 연결 풀, README 2-⑤·문제 해결.
   - 남은 확인: 사용자가 TiDB 인스턴스를 만들고 이클립스에서 접속 → `e2e.py` 통과 확인 (작업한 PC에는 JDK·톰캣이 없어 컴파일·실행 확인을 못 함).
   - TiDB의 AUTO_INCREMENT는 번호가 건너뛸 수 있음(기능 문제 없음). CHECK 제약은 기본 무시될 수 있으므로 rating 검사는 서버 코드에서도 한다(이미 함).
4. (선택) 인터넷 배포: GitHub Pages는 정적 파일만 가능해 이 앱을 실행할 수 없다. 배포하려면 `Dockerfile`(tomcat:10.1 이미지에 WAR 배포) + Render 같은 Docker 호스팅 + 클라우드 DB 조합. 사용자 확인 후 진행.
