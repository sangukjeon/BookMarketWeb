# DESIGN.md — 나만의 디자인 시스템

> **기준:** DOTCI 디자인 규칙(2026-10-11 확정 2차)
> **보강:** Codyssey Q&A · WSU 학식 · AI TrustMark에서 DOTCI에 없던 요소만 가져와 DOTCI 톤에 맞게 고쳤다.
> 값이 서로 다르면 **DOTCI 값이 이긴다.** 출처는 13장에 있다.
> 이 문서와 11장의 `tokens.css`는 값이 같아야 한다. 하나를 고치면 다른 하나도 고친다.

---

## 0. 원칙

1. **차분하고 깔끔한 모바일 우선.** 장식보다 정보 위계. 위계는 색이 아니라 **크기와 굵기**로 만든다.
   - 연회색 바탕 + 흰 카드, 1px 구분선, 넉넉한 둥근 모서리, 눌림 축소.
2. **파랑은 아껴 쓴다.** `--primary`로 **채운** 요소는 한 화면에 주 버튼 하나. 링크·선택 표시·아이콘에는 써도 된다.
3. **접근성은 공공 기준(KRDS 참고).** 대비 4.5:1, 누르는 영역 44px, 포커스 테두리는 지우지 않는다.
4. **비활성은 투명도가 아니라 색 교체.**
5. **아이콘은 Lucide 하나만.** 이모지 금지. 다른 회사의 이름·로고·브랜드 색·아이콘 폰트는 쓰지 않는다.
6. **서비스마다 특징은 하나만 남긴다.** (DOTCI: 잠긴 카드가 뒤집히며 열리는 순간) 나머지는 이 문서대로 조용하게 둔다.
7. **화면이 넓어져도 같은 언어.** PC는 폭과 단(column)만 늘린다. 색·모서리·글꼴은 그대로.

---

## 1. 색

### 1-1. 기본 (의미 토큰)

| 토큰 | 라이트 | 다크 | 쓰는 곳 |
|---|---|---|---|
| `--bg` | `#F2F3F8` | `#0F131A` | 화면 바탕 |
| `--surface` | `#FFFFFF` | `#181D26` | 상단 바, 카드, 시트 |
| `--surface-sub` | `#F9F9FB` | `#1E2430` | 표 머리, 목차, 카드 눌림 |
| `--chip` | `#F2F3F8` | `#222937` | 알약 바탕, 입력창 바탕 |
| `--line` | `#E5E8EF` | `#2A3140` | 1px 테두리·구분선 |
| `--line-weak` | `#F2F3F8` | `#222937` | 카드 **안쪽** 구분선 |
| `--text-strong` | `#141A24` | `#EEF1F6` | 제목, 강조 숫자 |
| `--text` | `#354153` | `#C9D0DB` | 본문 |
| `--text-2` | `#697383` | `#A3ACBB` | 보조 글자 (흰 바탕 4.79:1) |
| `--text-3` | `#99A1B1` | `#6F7889` | **자리표시 글자만.** 본문 금지 |
| `--primary` | `#2B6BF3` | `#4B83F7` | 링크, 선택 표시, 아이콘, 지도 마커 |
| `--primary-solid` | `#2B6BF3` | `#2B6BF3` | **흰 글자를 얹는 파란 바탕** (주 버튼, 내 말풍선) |
| `--primary-strong` | `#1E56D6` | `#8FB1FF` | 주 버튼 눌림, 연파랑 위 글자 |
| `--primary-weak` | `#EBF2FF` | `#1B2A4A` | 선택된 칩 바탕, 정보 띠, 현재 항목 |
| `--primary-tint` | `#DCE7FD` | `#22365E` | 2순위 버튼 바탕 |
| `--on-primary` | `#FFFFFF` | `#FFFFFF` | 파란 바탕 위 글자 |
| `--dim` | `rgba(20,26,36,.44)` | `rgba(0,0,0,.6)` | 시트·모달 뒤 어둡게 |
| `--pressed` | `rgba(20,26,36,.06)` | `rgba(255,255,255,.06)` | 목록 줄 눌림 덮개 |

> **왜 `--primary-solid`가 따로 있나:** 다크 모드의 `--primary`(`#4B83F7`)에 흰 글자를 얹으면 대비가 3.57:1로 기준에 못 미친다. 그래서 흰 글자가 올라가는 바탕은 두 테마 모두 `#2B6BF3`(4.67:1)로 고정한다. 라이트에서는 두 값이 같다.

> **주의:** 회색 바탕(`--bg`) 위의 `--text-2`는 4.33:1이다. 회색 바탕에 바로 놓는 보조 글자는 13px 이상 600 굵기로 쓰거나 흰 카드 안으로 옮긴다.

### 1-2. 상태 색

| 상태 | 아이콘·점 | 진한 글자 | 연한 바탕 | 쓰는 곳 |
|---|---|---|---|---|
| 성공·인증됨 | `--success` `#2F8A4E` | `--success-strong` `#1F7A47` | `--success-weak` `#EAF6EF` | "열었어요", 인증 완료 |
| 위험·취소됨 | `--danger` `#E5484D` | `--danger-strong` `#C8333A` | `--danger-weak` `#FDECEC` | NEW 배지, 오류, 삭제 |
| 주의·대기 | `--warn` `#F07800` | `--warn-strong` `#8A4300` | `--warn-weak` `#FFEFE0` | 검토 중, 경고 띠 |
| 정보 | `--primary` | `--primary-strong` | `--primary-weak` | 안내 띠 |

- 연한 바탕 위 글자는 **진한 글자 색**을 쓴다. `--success`나 `--danger`를 글자에 바로 쓰면 4.5:1이 안 나온다.
- 지울 때 쓰는 빨간 버튼은 `--danger-strong` 바탕 + 흰 글자(5.26:1).
- 다크 모드의 값은 11장 토큰에 있다.

### 1-3. 카테고리 (연한 바탕 / 아이콘 / 글자)

| 이름 | 키 | 바탕 | 아이콘 | 글자 | Lucide |
|---|---|---|---|---|---|
| 꿀팁 | `tip` | `#FFF5D1` | `#D99A00` | `#7A5600` | `Lightbulb` |
| 공부 | `study` | `#E5F2FD` | `#2D8CD6` | `#17507E` | `BookOpen` |
| 맛집 | `food` | `#FDEAF0` | `#E0577A` | `#8F2E48` | `Utensils` |
| 주의 | `warn` | `#FFEFE0` | `#F07800` | `#8A4300` | `TriangleAlert` |
| 수업 | `class` | `#EAF6DF` | `#4C9A1A` | `#2F6110` | `Pencil` |
| 규칙 | `rules` | `#EEF0FF` | `#5B5FD6` | `#34379A` | `Scale` |
| 기타 | `etc` | `#F2F3F8` | `#697383` | `#4A5667` | `Ellipsis` |

- 카테고리 키는 **색의 자리**다. 서비스마다 이름은 바꿔도 된다. (예: Codyssey의 출결=`tip` 자리, 과정=`study` 자리)
- 한 화면에 카테고리 색은 7개까지. 더 필요하면 분류를 다시 묶는다.
- 글자 대비는 모두 6:1 이상이다.

### 1-4. 그 밖의 색

| 토큰 | 값 | 쓰는 곳 |
|---|---|---|
| `--highlight` | `#FFF1B8` (다크 `#4A3F12`) | 검색어 강조 `<mark>` |
| `--toast-bg` | `--text-strong`과 같음 | 토스트 (다크에서는 `#2A3140`) |

---

## 2. 글꼴

**Pretendard Variable 하나만** 쓴다 (CDN 동적 서브셋). 굵기는 400 / 600 / 700 세 가지만.

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css" />
```

| 토큰 | 크기 / 줄높이 / 굵기 | 쓰는 곳 |
|---|---|---|
| `display` | 24 / 32 / 700 | **PC 전용** 구역 제목 |
| `heading` | 22 / 30 / 700 | 화면 제목, 장소 이름, 글 제목 |
| `title1` | 18 / 26 / 600 | 시트·모달 제목, 주 버튼, 강조 값 |
| `title2` | 16 / 24 / 600 | 상단 바 제목, 카드 제목, 질문 |
| `body1` | 16 / 26 / 400 | **긴 글** 본문 (공지 상세, 약관) |
| `body2` | 16 / 24 / 400 | 기본 본문 |
| `body3` | 14 / 22 / 400 | 카드 본문, 설명 |
| `label` | 14 / 22 / 600 | 입력창 이름표, 작은 버튼 |
| `help` | 13 / 20 / 400 | 도움말, 오류, 메타 정보 |
| `caption` | 12 / 18 / 600 | 칩, 태그, 표 머리, 출처 |

- **입력창 글자는 16px 이상.** 그보다 작으면 iOS가 화면을 확대한다.
- **숫자:** 표·순번·날짜·개수에는 `font-variant-numeric: tabular-nums`. 숫자는 굵게, 단위는 보통 (예: 도움 **12**명).
- **한글 줄바꿈:** `word-break: keep-all` + `overflow-wrap: anywhere`. 제목은 `text-wrap: balance`, 문단은 `text-wrap: pretty`.
- 영문 대문자 라벨(`STEP 1` 등)만 `letter-spacing: .08em`. 한글에는 자간을 주지 않는다.

---

## 3. 간격·레이아웃

### 3-1. 간격 (4px 단위)

| 토큰 | 값 | 쓰는 곳 |
|---|---|---|
| `--sp-1` | 4 | 제목-설명 사이 |
| `--sp-2` | 8 | 아이콘-글자, 칩 사이 |
| `--sp-3` | 12 | 카드 묶음 사이 |
| `--sp-4` | 16 | **화면 좌우 여백**, 카드 안쪽 |
| `--sp-6` | 24 | 구역 사이, PC 격자 간격 |
| `--sp-8` | 32 | 큰 구역 사이 |
| `--sp-12` | 48 | PC 화면 위아래 여백 |
| `--sp-16` | 64 | PC 좌우 여백 (넓은 화면) |

- 카드 목록 간격은 10px까지 허용한다 (DOTCI 쪽지 격자). 그 밖에는 위 단계만 쓴다.

### 3-2. 화면 폭

| 이름 | 최대 폭 | 쓰는 곳 |
|---|---|---|
| 앱 | 480px | 폰 앱형 화면 (DOTCI). 큰 화면에서는 가운데 정렬 |
| 읽기 | 720px | 글·목록 중심 화면 (Codyssey, 학식 560px도 허용) |
| 넓게 | 1120px | PC 2단 (목록 + 상세) |
| 대시보드 | 1440px | 표·통계가 많은 관리 화면 (TrustMark) |

### 3-3. 화면 크기 기준

| 구간 | 폭 | 규칙 |
|---|---|---|
| 폰 | ~640px | 1단. 플로팅 버튼은 아이콘만. 범주 격자 2열 |
| 태블릿 | 641~1023px | 1단 유지. 탭은 글자 길이만큼 (`flex: 0 0 auto`) |
| PC | 1024px~ | 2단 `420px + 나머지`, 간격 20px. 상세 칸은 `position: sticky`. 하단 시트는 오른쪽 아래 떠 있는 판(400px)이 된다 |

- 홈 화면 노치 대비: 상단 바에 `env(safe-area-inset-top)`, 하단 판·플로팅 버튼에 `env(safe-area-inset-bottom)`을 더한다.
- PC의 3·4열 격자: `repeat(3, 1fr)` / `repeat(4, 1fr)`, 간격 `--sp-6`. 768px 이하에서는 1열로 쌓는다.

---

## 4. 모서리

| 토큰 | 값 | 쓰는 곳 |
|---|---|---|
| `--r-tag` | 6px | 태그 |
| `--r-xs` | 8px | 건너뛰기 링크, 목차 항목 |
| `--r-sm` | 12px | 작은 버튼(40px), 표 테두리, 첨부·안내 상자 |
| `--r-control` | 14px | 입력창, 버튼, 띠, 토스트 |
| `--r-card` | 16px | 카드, 패널, 상세 화면 |
| `--r-sheet` | 24px | 하단 시트 위쪽, PC 떠 있는 판, 모달 |
| `--r-pill` | 999px | 칩, 배지, 상태 알약, 둥근 아이콘 버튼 |

- 바깥 모서리가 16px이면 안쪽 요소는 12px 이하로 둔다. 안이 바깥보다 둥글면 어색하다.

---

## 5. 그림자

평소에는 **그림자 대신 1px `--line`** 으로 나눈다. 그림자는 무언가 떠 있을 때만 쓴다.

| 토큰 | 값 | 쓰는 곳 |
|---|---|---|
| `--shadow-sm` | `0 1px 2px rgba(20,26,36,.06)` | 카드 (필요할 때만) |
| `--shadow-fab` | `0 2px 8px rgba(20,26,36,.12)` | 작은 플로팅 버튼 (40px) |
| `--shadow-float` | `0 4px 12px rgba(20,26,36,.16)` | 지도 위 버튼·마커, 챗봇 버튼 |
| `--shadow-sheet` | `0 0 20px rgba(0,0,0,.18)` | 하단 시트, PC 떠 있는 판 |
| `--shadow-modal` | `0 20px 60px rgba(20,26,36,.24)` | 가운데 모달 |
| `--ring-current` | `0 0 0 3px var(--primary-weak)` | "지금" 강조 카드 (테두리 `--primary`와 함께) |

다크 모드에서는 그림자가 잘 안 보인다. 떠 있는 요소에 1px `--line` 테두리를 함께 둔다.

---

## 6. 움직임

기본 곡선은 `--ease: cubic-bezier(.42, 0, .58, 1)`이다.

| 순간 | 움직임 | 시간 |
|---|---|---|
| 누름 | `scale(.96)` / 작은 요소 `.92` / 넓은 줄·카드 `.98` | 100ms |
| 목록 줄 누름 | 축소 + `--pressed` 덮개 | 100ms |
| 탭 밑줄 이동 | `transform` + `width` | 150ms |
| 펼침 화살표 | `rotate(180deg)` | 150ms |
| 하단 시트 | 아래에서 올라옴 `translateY(100%→0)` | 240ms |
| PC 떠 있는 판·모달 | `translateY(12~16px)` + 페이드 | 150ms |
| 카드 뒤집기 (DOTCI 전용) | `rotateY(180deg)`, 하나씩 120ms 간격 | 500ms |
| 하트 | 1.3배 → 1배 | 200ms |
| 입력 중 점 3개 | 투명도 .3 ↔ 1, 150ms씩 늦게 | 1s 반복 |
| 새로고침 아이콘 | 회전 | 0.7s 반복 |

- **마우스 올림(hover)은 PC에서만:** `@media (hover: hover)` 안에서 테두리를 `--primary`로 바꾸거나 바탕을 `--primary-strong`으로 바꾼다. 커지는 효과는 쓰지 않는다.
- `prefers-reduced-motion: reduce`이면 모든 전환을 1ms로 줄이고 누름 축소를 끈다. 뒤집기는 페이드로 바꾼다.

---

## 7. 컴포넌트

### 7-1. 버튼

| 종류 | 높이 | 모양 | 쓰는 곳 |
|---|---|---|---|
| 주 버튼 `.btn-main` | 56 | `--primary-solid` + 흰 글자 title1, `--r-control` | 화면 하단 흰 판 위에 고정. **한 화면에 하나** |
| 2순위 버튼 `.btn-tint` | 48 | `--primary-tint` + `--primary-strong` 글자 | 주 버튼 옆의 덜 중요한 행동 |
| 보조 버튼 `.btn-sub` | 48 | 흰 바탕 + 1px `--line` + `--text` 글자 600 | 취소, 목록으로 |
| 작은 버튼 `.btn-s` | 40 | 위 세 종류 중 하나, label, `--r-sm` | 카드·표 안 |
| 위험 버튼 `.btn-danger` | 48 | `--danger-strong` + 흰 글자 | 지우기, 탈퇴 |
| 글자 버튼 `.text-btn` | 44 (영역) | `--primary` 글자 15/600 | 더보기, 다시 보내기 |
| 아이콘 버튼 `.icon-btn` | 44×44 | 둥근 투명 바탕, `aria-label` 필수 | 상단 바 |

- **비활성:** 주 버튼은 `--line` 바탕 + `--text-3` 글자. 보조 버튼은 `--bg` 바탕 + `--text-3` 글자. 커서는 `not-allowed`, 누름 축소는 끈다.
- **눌림:** 주 버튼은 `--primary-strong` 바탕. 나머지 버튼은 `::after`에 `--pressed` 덮개를 깐다.
- 버튼 문구는 동사로 시작하고 마침표·느낌표를 붙이지 않는다: `도착 확인하기`, `질문 남기기`.

### 7-2. 플로팅 버튼

- **큰 버튼(챗봇 등):** 높이 56, 둥근 알약, `--primary-solid`, 흰 글자 + 아이콘, `--shadow-float`. 오른쪽·아래 16px. 폰에서는 글자를 숨기고 56×56 원으로.
- **작은 버튼(테마·새로고침):** 40×40 원, 흰 바탕 + 1px `--line`, `--text-2` 아이콘 18px, `--shadow-fab`. 세로로 쌓고 간격 8.
- 플로팅 버튼이 있으면 화면 맨 아래 여백을 `버튼 높이 + 16px` 이상 둔다. 글이 가리지 않게.

### 7-3. 입력

- **입력 칸 `.field`:** 세로로 이름표(label) → 입력창 → 도움말(help) 또는 오류(help, `--danger-strong`). 간격 6, 칸 사이 16.
- **입력창 `.input`:** 최소 높이 48, 안쪽 여백 `12 14`, `--r-control`, `--chip` 바탕 + 투명 테두리. 포커스하면 `--surface` 바탕 + `--primary` 테두리. 오류이면 `--danger` 테두리.
- **여러 줄 `.textarea`:** 최소 112, 세로로만 늘어남.
- **검색창 `.search`:** 높이 48, 왼쪽 돋보기(`--text-2`), 오른쪽 지우기 버튼(24px 원, `--text-3` 바탕 + 흰 X). 포커스 안에 있으면 테두리를 `--primary-tint`로.
- 자리표시 글자만 `--text-3`.

### 7-4. 칩 `.chip`

- 높이 40 (빽빽한 줄은 36), 좌우 14, `--r-pill`, 흰 바탕 + 1px `--line`, `--text-2` 글자 15/600.
- 선택됨(`aria-pressed="true"`): `--primary` 테두리 + `--primary-weak` 바탕 + `--primary-strong` 글자.
- 칩 안 개수는 caption + tabular-nums.
- 가로로 넘치면 `.chips.scroll`: 줄바꿈 없이 가로 스크롤, 스크롤바 숨김, 좌우를 화면 끝까지 붙인다(`margin: 0 -16px; padding: 0 16px`).

### 7-5. 탭 `.tabs`

- 밑줄 탭. 높이 48(최소 44), 글자 16/600 `--text-2`. 아래 1px `--line`.
- 선택됨(`aria-selected="true"`): 글자 `--text-strong` + 아래 2px `--primary` 밑줄.
- 폰에서 탭이 4개 이하면 칸을 똑같이 나눈다(`flex: 1`). 많으면 가로 스크롤.
- 밑줄을 움직이고 싶으면 따로 막대(`.tab-bar`)를 두고 150ms로 옮긴다.

### 7-6. 날짜 고르기 `.dates`

- 7일이 폰에서도 한 줄: 칸마다 `flex: 1 0 42px`, 최대 64, 높이 60, `--r-control`, 흰 바탕 + 1px `--line`.
- 위는 요일(caption), 아래는 날짜(18/700 tabular-nums).
- 오늘: 요일 글자만 `--primary`. 선택됨: `--primary-solid` 바탕 + 흰 글자.

### 7-7. 카드

| 종류 | 규칙 |
|---|---|
| 기본 `.card` | 흰 바탕, 1px `--line`, `--r-card`, 안쪽 16 |
| 지금 강조 `.card--now` | 테두리 `--primary` + `--ring-current` |
| 선택됨 (PC 2단) `[aria-current="true"]` | 테두리 `--primary` + `--primary-weak` 바탕 |
| 패널 안 목록 | 카드 모서리·여백 0, 줄 사이 1px `--line-weak` |
| 쪽지 카드 (DOTCI) | 2열 격자, 높이 144, 안쪽 14. 잠김 = 카테고리 바탕, 열림 = 흰 바탕 + 카테고리 칩 |
| 단계 카드 (PC 소개) | 흰 바탕, 1px `--line`, `--r-card`, 안쪽 `28 24`. 위에 44px 아이콘 상자(`--primary-weak` 바탕, `--r-xs`) + `STEP 1` 라벨. hover 시 테두리 `--primary` |

- 카드 메타 줄: help 크기, `--text-2`, 항목 사이 12. 끝에 오른쪽 화살표(`ChevronRight`, `--text-3`).
- 카드 누름: `scale(.98)` + `--surface-sub` 바탕.

### 7-8. 태그·상태 알약·배지

- **태그 `.tag`:** 안쪽 `3 8`, `--r-tag`, caption. 연한 바탕 + 진한 글자 (카테고리 또는 상태).
- **상태 알약 `.pill`:** 안쪽 `6 12`, `--r-pill`, 13/700. 앞에 6px 점. 상태 색 표(1-2)의 연한 바탕 + 진한 글자 + 아이콘 색 점.
  - 예: `● 인증됨`(성공) / `● 검토 중`(주의) / `● 취소됨`(위험)
  - 색만으로 상태를 알리지 않는다. 글자가 꼭 함께 있어야 한다.
- **숫자 배지:** 지름 18 이상 원, `--danger` 바탕 + 흰 글자 11/700. 지도 마커 오른쪽 위.
- **"지금" 배지 `.now`:** 높이 20, 좌우 8, `--r-pill`, `--primary-solid` 바탕 + 흰 글자 11/700.
- **곁들임 알약:** 안쪽 `3 10`, `--r-pill`, `--chip` 바탕, 13 `--text` (회색 바탕 위 `--text-2`는 4.5:1이 안 된다). 주 정보 아래 덜 중요한 항목을 나열할 때.

### 7-9. 안내

- **띠 `.banner`:** 안쪽 `12 16`, `--r-control`, body3. 왼쪽 아이콘(위로 2px). `--info`는 `--primary-weak` + `--primary-strong`, `--warn`은 `--warn-weak` + `--warn-strong`, `--danger`는 `--danger-weak` + `--danger-strong`.
- **알림 상자 `.notice-box`:** 흰 바탕 + 1px `--line`, `--r-sm`, 안쪽 `14 16`, help `--text-2`. 비공식 안내·면책 문구.
- **첨부 상자 `.attach`:** `--chip` 바탕(정보성이면 `--primary-weak`), `--r-sm`, 안쪽 `14 16`. 제목은 13/600 `--text`.
- **토스트:** 하단 판 위 16px, 가운데. `--toast-bg` + 흰 글자 15/22, `--r-control`, 안쪽 `12 16`. 아이콘 + 한 문장. 2~3초 뒤 사라진다. `role="status"`.

### 7-10. 상단 바 · 내비게이션 · 바닥글

- **상단 바:** 높이 52 (+ safe-area), 흰 바탕, 아래 1px `--line`, `position: sticky`. 제목 title2. 좌우 아이콘 버튼 44.
- **PC 내비게이션:** 3칸 격자(`1fr auto 1fr`) — 왼쪽 로고, 가운데 링크(15/600 `--text`, 간격 32, hover `--primary`), 오른쪽 버튼. 좌우 여백 64. 768px 이하에서는 링크를 숨기고 메뉴 버튼으로.
- **바닥글:** caption `--text-2`. 플로팅 버튼이 있으면 아래 여백 88 이상. (PC 소개 페이지는 `--text-strong` 바탕 + `--line` 글자로 어둡게 해도 된다.)

### 7-11. 하단 시트 · 모달

- **하단 시트 (폰):** 흰 바탕, 위 모서리 `--r-sheet`, 손잡이 36×4 `--line`(위 8), `--shadow-sheet`, 높이 최대 `min(90vh, 720px)`. 뒤는 `--dim`. 240ms.
- **떠 있는 판 (PC 1024~):** 시트 대신 오른쪽 아래 24px 띄움, 폭 400, 모서리 전부 `--r-sheet`, 손잡이 숨김, 뒤를 어둡게 하지 않음.
- **모달 (가운데):** 확인·입력이 꼭 필요할 때만. 흰 바탕, `--r-sheet`, 안쪽 28, 폭 440(최대 90vw), `--shadow-modal`, 뒤 `--dim`. 제목 title1 → 설명 body3 `--text-2` → 내용 → 오른쪽 정렬 버튼 줄(간격 8, 보조 + 주). 폰에서는 하단 시트로 바꾼다.
- 열리면 포커스를 안으로 옮기고, 닫히면 연 버튼으로 돌려준다. `Esc`로 닫힌다.

### 7-12. 대화 (챗봇)

- **머리:** 40px 둥근 아바타(`--primary-weak` 바탕 + `--primary` 아이콘) + 제목 title1 + 설명 help.
- **대화 영역:** `--bg` 바탕, 안쪽 16, 말풍선 사이 12. 말풍선 최대 폭 92%.
- **말풍선:** 안쪽 `12 14`, `--r-card`, 15/23.
  - 상대: 흰 바탕 + `--text`, **왼쪽 위 모서리만 4px.**
  - 나: `--primary-solid` + 흰 글자, **오른쪽 위 모서리만 4px.**
- **말풍선 안 링크 버튼:** 최소 40, `--r-sm`, `--chip` 바탕, 13/600. 첫 번째(가장 맞는 답)만 `--primary-weak` + `--primary`.
- **입력 줄:** 입력창(48, `--chip` 바탕) + 48px 둥근 보내기 버튼(`--primary-solid`). 아래에 safe-area 여백.
- **입력 중:** 6px 회색 점 3개 깜빡임.

### 7-13. 펼침 목록 (FAQ)

- 질문 줄 전체가 버튼(`aria-expanded`). 질문은 title2, 오른쪽에 `ChevronDown`(`--text-3`), 펼치면 180도.
- 줄 사이 1px `--line-weak`. 누르면 `scale(.98)` + `--pressed`.
- 답은 body3 `--text`, `--surface-sub` 상자 안. 검색으로 찾아온 항목은 잠깐 `--primary-weak` 바탕으로 깜빡인다.

### 7-14. 표

- 바깥을 `.tbl-wrap`으로 감싼다: 1px `--line`, `--r-sm`, 흰 바탕, `overflow-x: auto` (폰에서 가로 스크롤).
- 칸 안쪽 `10 12`, 14/21, 줄 사이 1px `--line-weak`.
- 머리: `--surface-sub` 바탕, caption `--text-2`, 줄바꿈 없음.
- 줄 머리(`tbody th`): 600 `--text-strong`. 숫자 열은 오른쪽 정렬 + tabular-nums.
- 넓은 관리 표(PC): 칸 안쪽 `14 24`. 비었을 때는 한 줄을 합쳐 가운데에 help `--text-2` 문구.

### 7-15. 비었을 때 · 불러오는 중

- **빈 화면:** 가운데 정렬, 안쪽 `40 20`. 56px 원(`--chip` 바탕 + `--text-2` Lucide 아이콘) → 제목 title2 → 설명 body3 `--text-2` → (있으면) 보조 버튼 하나.
- **PC 2단에서 상세를 아직 안 골랐을 때:** 점선 1px `--line` 상자, 최소 높이 320, 안내 한 줄.
- **뼈대(skeleton):** 실제 카드와 같은 크기·모서리의 흰 상자 + 1px `--line`. 반짝임 애니메이션은 넣지 않는다 (동작 줄이기 설정을 지키기 쉽다). `aria-busy="true"`.
- 불러오는 버튼은 아이콘을 0.7s로 돌린다.

### 7-16. 긴 글 목차 · 검색 강조

- **목차 `.toc`:** `--surface-sub` 바탕, 1px `--line-weak`, `--r-sm`, 안쪽 `12 14`. 항목은 최소 40, 앞에 22px 원 번호(`--primary-weak` 바탕 + `--primary` 12/700).
- 목차로 이동하는 제목에는 `scroll-margin-top`을 상단 바 높이 이상으로 준다.
- **검색어 강조:** `<mark>`에 `--highlight` 바탕, 글자색 그대로, 모서리 3px.

### 7-17. 지도 마커 (DOTCI)

- 지름 36 원, `--primary-solid` 바탕 + 흰 아이콘, `--shadow-float`. 오른쪽 위에 숫자 배지. 이미 연 장소는 `--success`.
- 지도를 못 쓰는 경우(키 없음, 화면 읽기)를 위해 **목록 보기**를 함께 둔다.

---

## 8. 로고

- **기본형:** 서비스 이름(Pretendard 700) 오른쪽 아래에 `--primary` 점 하나. 점 지름은 글자 크기의 약 0.3배(`0.3em`), 글자와 간격 `0.08em`.
- 다른 서비스도 이 형식을 빌려 쓸 수 있다: `글자 + 파란 점`.
- 로고 옆에 부제를 붙일 때는 `|` 구분선(`--line`)을 두고 부제는 title2 `--text-2`. 글자 기준선을 맞춘다.

---

## 9. 문구

- 말투는 "~요" 정중체. 짧게 쓴다.
- 버튼은 동사로 시작하고 마침표·느낌표 없이: `도착 확인하기`, `흔적 남기기`, `다시 불러오기`.
- 오류는 **무엇이 안 됐는지 + 어떻게 하면 되는지**: "위치를 확인하지 못했어요. 위치 권한을 켜 주세요."
- 빈 화면은 비난하지 않는다: "아직 쪽지가 없어요" (O) / "쪽지 없음" (X).
- 숫자 + 단위는 붙여 쓴다: `12명`, `3분 전`.
- 비공식 서비스는 바닥글이나 알림 상자에 그 사실을 한 줄로 밝힌다.

---

## 10. 접근성 (KRDS 참고)

- **키보드 포커스:** `:focus-visible`에 2px `--primary` 테두리 + 2px 간격. `outline: none` 금지. (입력창은 테두리색 변화도 함께)
- **누르는 영역** 최소 44×44.
- **글자 대비** 4.5:1 이상, 아이콘·테두리는 3:1 이상. 이 문서의 색 조합은 모두 계산해서 확인했다.
- **비활성은 색 교체.** 투명도를 쓰지 않는다.
- **건너뛰기 링크:** 화면 맨 앞에 "본문으로 건너뛰기". 평소에는 화면 밖에 두고, 포커스하면 왼쪽 위 12px에 나타난다(`--text-strong` 바탕 + 흰 글자, `--r-xs`).
- **상태는 속성으로:** 칩·날짜 `aria-pressed`, 탭 `role="tab"` + `aria-selected`, 펼침 `aria-expanded`, 현재 항목 `aria-current`, 불러오는 중 `aria-busy`, 토스트 `role="status"`.
- 아이콘만 있는 버튼에는 `aria-label`. 꾸밈 아이콘에는 `aria-hidden="true"`.
- 카드는 읽히는 문장을 따로 준다: "잠긴 쪽지, 꿀팁, 4학년, 도움 12명".
- **다크 모드:** `:root[data-theme="dark"]`로 바꾼다. 사용자가 고른 값은 `localStorage`에 저장하고, `<head>`의 작은 스크립트에서 먼저 적용해 깜빡임을 막는다. 처음에는 `prefers-color-scheme`을 따른다. 해·달 버튼은 **바뀔 테마**의 아이콘을 보여 준다.
- **동작 줄이기:** `prefers-reduced-motion: reduce`이면 전환을 1ms로, 누름 축소를 끈다.

---

## 11. 코드 토큰

### 11-1. `tokens.css`

```css
/* tokens.css — DESIGN.md와 값이 같아야 한다 */
:root {
  color-scheme: light;

  /* 글꼴 */
  --font: "Pretendard Variable", Pretendard, -apple-system, BlinkMacSystemFont, system-ui,
    "Apple SD Gothic Neo", "Malgun Gothic", "Noto Sans KR", sans-serif;

  /* 회색 단계 (직접 쓰지 말고 아래 의미 토큰을 쓴다) */
  --gray-0: #ffffff;
  --gray-50: #f9f9fb;
  --gray-100: #f2f3f8;
  --gray-200: #e5e8ef;
  --gray-300: #cbd1dc;
  --gray-400: #b4bbcb;
  --gray-500: #99a1b1;
  --gray-600: #697383;
  --gray-800: #354153;
  --gray-900: #262f3c;
  --gray-1000: #141a24;

  /* 바탕·선 */
  --bg: var(--gray-100);
  --surface: var(--gray-0);
  --surface-sub: var(--gray-50);
  --chip: var(--gray-100);
  --line: var(--gray-200);
  --line-weak: var(--gray-100);

  /* 글자 */
  --text-strong: var(--gray-1000);
  --text: var(--gray-800);
  --text-2: var(--gray-600);
  --text-3: var(--gray-500);

  /* 파랑 */
  --primary: #2b6bf3;
  --primary-solid: #2b6bf3;
  --primary-strong: #1e56d6;
  --primary-weak: #ebf2ff;
  --primary-tint: #dce7fd;
  --on-primary: #ffffff;

  /* 상태 */
  --success: #2f8a4e;
  --success-strong: #1f7a47;
  --success-weak: #eaf6ef;
  --danger: #e5484d;
  --danger-strong: #c8333a;
  --danger-weak: #fdecec;
  --warn: #f07800;
  --warn-strong: #8a4300;
  --warn-weak: #ffefe0;

  /* 카테고리: 바탕 / 아이콘 / 글자 */
  --cat-tip-bg: #fff5d1;   --cat-tip-icon: #d99a00;   --cat-tip-text: #7a5600;
  --cat-study-bg: #e5f2fd; --cat-study-icon: #2d8cd6; --cat-study-text: #17507e;
  --cat-food-bg: #fdeaf0;  --cat-food-icon: #e0577a;  --cat-food-text: #8f2e48;
  --cat-warn-bg: #ffefe0;  --cat-warn-icon: #f07800;  --cat-warn-text: #8a4300;
  --cat-class-bg: #eaf6df; --cat-class-icon: #4c9a1a; --cat-class-text: #2f6110;
  --cat-rules-bg: #eef0ff; --cat-rules-icon: #5b5fd6; --cat-rules-text: #34379a;
  --cat-etc-bg: #f2f3f8;   --cat-etc-icon: #697383;   --cat-etc-text: #4a5667;

  /* 그 밖 */
  --dim: rgba(20, 26, 36, 0.44);
  --pressed: rgba(20, 26, 36, 0.06);
  --highlight: #fff1b8;
  --toast-bg: var(--gray-1000);

  /* 간격 */
  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-6: 24px;
  --sp-8: 32px;
  --sp-12: 48px;
  --sp-16: 64px;

  /* 모서리 */
  --r-tag: 6px;
  --r-xs: 8px;
  --r-sm: 12px;
  --r-control: 14px;
  --r-card: 16px;
  --r-sheet: 24px;
  --r-pill: 999px;

  /* 그림자 */
  --shadow-sm: 0 1px 2px rgba(20, 26, 36, 0.06);
  --shadow-fab: 0 2px 8px rgba(20, 26, 36, 0.12);
  --shadow-float: 0 4px 12px rgba(20, 26, 36, 0.16);
  --shadow-sheet: 0 0 20px rgba(0, 0, 0, 0.18);
  --shadow-modal: 0 20px 60px rgba(20, 26, 36, 0.24);
  --ring-current: 0 0 0 3px var(--primary-weak);

  /* 크기 */
  --bar-h: 52px;
  --btn-main-h: 56px;
  --btn-sub-h: 48px;
  --btn-s-h: 40px;
  --hit: 44px;
  --plate-h: calc(var(--btn-main-h) + 2 * var(--sp-3) + 1px);

  /* 움직임 */
  --ease: cubic-bezier(0.42, 0, 0.58, 1);
  --dur-press: 100ms;
  --dur-fast: 150ms;
  --dur-sheet: 240ms;
}

:root[data-theme="dark"] {
  color-scheme: dark;

  --bg: #0f131a;
  --surface: #181d26;
  --surface-sub: #1e2430;
  --chip: #222937;
  --line: #2a3140;
  --line-weak: #222937;

  --text-strong: #eef1f6;
  --text: #c9d0db;
  --text-2: #a3acbb;
  --text-3: #6f7889;

  --primary: #4b83f7;
  --primary-solid: #2b6bf3; /* 흰 글자 대비 4.67:1 유지 */
  --primary-strong: #8fb1ff;
  --primary-weak: #1b2a4a;
  --primary-tint: #22365e;

  --success: #4fb872;
  --success-strong: #7fd39b;
  --success-weak: #17301f;
  --danger: #ff6b6f;
  --danger-strong: #ff9ea1;
  --danger-weak: #3a1d20;
  --warn: #ffa14a;
  --warn-strong: #ffbe80;
  --warn-weak: #3a2814;

  --cat-tip-bg: #3f361e;   --cat-tip-icon: #e1ae33;   --cat-tip-text: #eed28c;
  --cat-study-bg: #1c3349; --cat-study-icon: #57a3de; --cat-study-text: #a0cbed;
  --cat-food-bg: #402937;  --cat-food-icon: #e67995;  --cat-food-text: #f1b3c3;
  --cat-warn-bg: #432f1e;  --cat-warn-icon: #f39333;  --cat-warn-text: #f8c28c;
  --cat-class-bg: #223624; --cat-class-icon: #70ae48; --cat-class-text: #aed298;
  --cat-rules-bg: #252a49; --cat-rules-icon: #7c7fde; --cat-rules-text: #b5b7ed;
  --cat-etc-bg: #282e39;   --cat-etc-icon: #878f9c;   --cat-etc-text: #bcc0c7;

  --dim: rgba(0, 0, 0, 0.6);
  --pressed: rgba(255, 255, 255, 0.06);
  --highlight: #4a3f12;
  --toast-bg: #2a3140;
}
```

> 다크 모드에서 위험 버튼(`.btn-danger`)은 `--danger-strong`이 밝은 분홍이 되므로 바탕을 `#C8333A`로 고정하고 흰 글자를 쓴다.

### 11-2. 기본 스타일 (모든 프로젝트 공통)

```css
*, *::before, *::after { box-sizing: border-box; }
html, body { margin: 0; }
body {
  font-family: var(--font);
  font-size: 16px; line-height: 24px;
  color: var(--text); background: var(--bg);
  -webkit-font-smoothing: antialiased;
  -webkit-tap-highlight-color: transparent;
  word-break: keep-all; overflow-wrap: anywhere;
}
h1, h2, h3 { margin: 0; text-wrap: balance; }
p { margin: 0; }
p, li { text-wrap: pretty; }
button { font: inherit; color: inherit; border: 0; background: none; padding: 0; cursor: pointer; }
:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
.pressable { transition: transform var(--dur-press) var(--ease); }
.pressable:active { transform: scale(.96); }
.sr-only { position: absolute; width: 1px; height: 1px; margin: -1px; padding: 0;
  overflow: hidden; clip: rect(0 0 0 0); white-space: nowrap; border: 0; }
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { transition-duration: 1ms !important; animation-duration: 1ms !important; }
}
```

### 11-3. 다크 모드 깜빡임 막기 (`<head>` 안)

```html
<script>
  try {
    var t = localStorage.getItem('theme')
    if (t === 'dark' || (!t && matchMedia('(prefers-color-scheme: dark)').matches))
      document.documentElement.dataset.theme = 'dark'
  } catch (e) {}
</script>
```

### 11-4. Tailwind를 쓸 때

```js
// tailwind.config.js — 값은 CSS 변수에서 가져온다 (다크 모드가 자동으로 따라온다)
export default {
  darkMode: ['selector', '[data-theme="dark"]'],
  theme: {
    screens: { md: '641px', lg: '1024px' },
    colors: {
      transparent: 'transparent',
      bg: 'var(--bg)', surface: 'var(--surface)', 'surface-sub': 'var(--surface-sub)',
      chip: 'var(--chip)', line: 'var(--line)', 'line-weak': 'var(--line-weak)',
      'text-strong': 'var(--text-strong)', text: 'var(--text)', 'text-2': 'var(--text-2)', 'text-3': 'var(--text-3)',
      primary: { DEFAULT: 'var(--primary)', solid: 'var(--primary-solid)', strong: 'var(--primary-strong)',
                 weak: 'var(--primary-weak)', tint: 'var(--primary-tint)', on: 'var(--on-primary)' },
      success: { DEFAULT: 'var(--success)', strong: 'var(--success-strong)', weak: 'var(--success-weak)' },
      danger:  { DEFAULT: 'var(--danger)',  strong: 'var(--danger-strong)',  weak: 'var(--danger-weak)' },
      warn:    { DEFAULT: 'var(--warn)',    strong: 'var(--warn-strong)',    weak: 'var(--warn-weak)' },
    },
    fontFamily: { sans: 'var(--font)' },
    fontSize: {
      display: ['24px', { lineHeight: '32px', fontWeight: '700' }],
      heading: ['22px', { lineHeight: '30px', fontWeight: '700' }],
      title1:  ['18px', { lineHeight: '26px', fontWeight: '600' }],
      title2:  ['16px', { lineHeight: '24px', fontWeight: '600' }],
      body1:   ['16px', { lineHeight: '26px' }],
      body2:   ['16px', { lineHeight: '24px' }],
      body3:   ['14px', { lineHeight: '22px' }],
      label:   ['14px', { lineHeight: '22px', fontWeight: '600' }],
      help:    ['13px', { lineHeight: '20px' }],
      caption: ['12px', { lineHeight: '18px', fontWeight: '600' }],
    },
    fontWeight: { normal: '400', semibold: '600', bold: '700' },
    spacing: { 0: '0', 1: '4px', 2: '8px', 3: '12px', 4: '16px', 6: '24px', 8: '32px', 12: '48px', 16: '64px' },
    borderRadius: { none: '0', tag: '6px', xs: '8px', sm: '12px', control: '14px', card: '16px', sheet: '24px', pill: '999px' },
    boxShadow: { none: 'none', sm: 'var(--shadow-sm)', fab: 'var(--shadow-fab)', float: 'var(--shadow-float)',
                 sheet: 'var(--shadow-sheet)', modal: 'var(--shadow-modal)', current: 'var(--ring-current)' },
    extend: {
      maxWidth: { app: '480px', read: '720px', wide: '1120px', dash: '1440px' },
      transitionTimingFunction: { DEFAULT: 'cubic-bezier(.42,0,.58,1)' },
    },
  },
}
```

---

## 12. AI에게 줄 때 (짧은 지침)

> 아래 블록을 그대로 복사해서 프롬프트 앞에 붙인다.

```text
[디자인 규칙 — 반드시 지킬 것]
- 모바일 우선, 차분하고 깔끔하게. 연회색 바탕(#F2F3F8) + 흰 카드 + 1px 선(#E5E8EF). 그림자는 떠 있는 것에만.
- 글꼴은 Pretendard Variable 하나, 굵기 400/600/700만. 위계는 크기와 굵기로.
  제목 22/30/700, 카드 제목 16/24/600, 본문 16/24, 카드 본문 14/22, 칩·태그 12/18/600. 입력창 글자 16px 이상.
- 파랑 #2B6BF3은 링크·선택 표시용. 파랑으로 채운 버튼은 한 화면에 하나(높이 56, 모서리 14, 화면 하단 고정).
  보조 버튼은 높이 48, 흰 바탕 + 1px 선.
- 글자색: 제목 #141A24, 본문 #354153, 보조 #697383, 자리표시 글자만 #99A1B1.
- 상태: 성공 #1F7A47/#EAF6EF, 위험 #C8333A/#FDECEC, 주의 #8A4300/#FFEFE0 (글자/바탕). 색만으로 알리지 말고 글자를 함께.
- 간격은 4px 단위(4/8/12/16/24/32), 화면 좌우 여백 16. 모서리: 태그 6, 버튼·입력창 14, 카드 16, 시트 24, 칩 999.
- 누르면 scale(.96) 100ms. 하단 시트는 240ms로 올라옴. prefers-reduced-motion을 지킨다.
- 접근성: 포커스 테두리 2px 파랑 + 2px 간격(outline:none 금지), 누르는 영역 44px 이상, 대비 4.5:1 이상,
  비활성은 투명도 대신 회색 바탕 + #99A1B1 글자, 아이콘 버튼에 aria-label.
- 아이콘은 Lucide만. 이모지 금지. 한글은 word-break: keep-all.
- 문구는 "~요" 체. 버튼은 동사로 시작하고 마침표 없이("질문 남기기").
- 다크 모드는 :root[data-theme="dark"]로 바꾸고, 흰 글자를 얹는 파란 바탕은 다크에서도 #2B6BF3 유지.
- PC(1024px~)에서는 폭과 단만 늘린다(목록 420px + 상세). 색·모서리·글꼴은 그대로.
```

---

## 13. 출처 (어디서 가져왔나)

| 요소 | 출처 | 바꾼 점 |
|---|---|---|
| 원칙, 기본 색, 카테고리 5색, 글꼴 단계, 모서리, 그림자 3종, 움직임, 접근성, 주 버튼·보조 버튼·칩·입력·띠·토스트·하단 시트·로고·문구 | **DOTCI** | 기준. 그대로 둠 |
| 회색 단계 전체, `--surface-sub`, `--pressed`, `--ease`, 2순위 버튼(`--primary-tint`), 작은 버튼 40px | Codyssey Q&A | 이름을 DOTCI 방식(`--text-2` 등)으로 맞춤 |
| 카테고리 `rules`(보라)·`etc`(회색) | Codyssey Q&A | — |
| 밑줄 탭, 검색창, 펼침 목록, 표, 빈 화면, 목차, 검색어 강조, 건너뛰기 링크, 챗봇(말풍선·입력 중 점), PC 2단·떠 있는 판, `body1` | Codyssey Q&A | 버튼 높이 46 → 48, 누름 .92 → .96 (DOTCI 값) |
| 다크 모드 기본 색, 테마 저장 방식, 날짜 고르기, "지금" 강조 카드, 곁들임 알약, 작은 플로팅 버튼, 뼈대 화면, safe-area | WSU 학식 | 흰 글자 대비가 모자라 `--primary-solid` 추가. 상태·카테고리 다크 값을 새로 계산 |
| 상태 알약(점 + 글자), 모달, 단계 카드, PC 내비게이션, 3·4열 격자, 큰 간격 48·64, 대시보드 폭 1440, `display` 글꼴, hover 규칙 | AI TrustMark | 색은 DOTCI 팔레트로, 모서리 6~10 → 12~24로, 버튼 굵기 700 → 600으로 바꿈 |
| 성공·위험·주의의 진한 글자색 | 새로 정함 | 원래 값이 연한 바탕 위에서 4.5:1이 안 돼서 진한 색을 추가함 |

### 버린 것

- AI TrustMark의 자체 파랑 `#256EF4`, 회색 단계, 보라 `--color-chain` → DOTCI 파랑·회색과 겹쳐서 버림
- AI TrustMark의 플로팅 버튼 hover 확대 `scale(1.06)` → "커지는 hover 없음" 규칙과 충돌해서 버림
- WSU 학식 빈 화면의 큰 글자 아이콘 → 이모지 금지 규칙에 따라 Lucide 아이콘으로 바꿈
---

## 부록. BookMarketWeb 적용 기록

- **원칙 5의 예외: 소셜 로그인 버튼 3개(카카오·네이버·Google).** 각 회사의 로그인 버튼 가이드가 로고와 바탕색을 정해 두었으므로 그대로 따른다.
  - 카카오 `#FEE500` 바탕 + 검정 말풍선 + `rgba(0,0,0,.85)` 글자 / 네이버 `#03C75A` 바탕 + 흰 N + 흰 글자 / Google 흰 바탕 + `#747775` 1px 테두리 + 4색 G (다크: `#131314` 바탕, `#8E918F` 테두리)
  - 높이 56, 모서리 14, Pretendard 16/600, 누름 `scale(.98)`은 이 문서 값을 쓴다. 로고는 왼쪽 20px, 글자는 가운데.
  - 파란 주 버튼이 아니므로 "주 버튼은 한 화면에 하나" 규칙과 겹치지 않는다.
- 로그인 화면은 소셜 버튼을 먼저 두고, 아이디 로그인은 "다른 방법으로 계속하기" 아래 동그란 버튼(56px, 흰 바탕 + 1px 선)으로 펼친다.
- 도서 분야 색: `IT전문서` = `study` 자리, `컴퓨터입문` = `class` 자리, 새 분야는 `rules`/`tip` 자리.
