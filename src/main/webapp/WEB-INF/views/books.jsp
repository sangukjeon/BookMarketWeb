<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="${empty q ? '도서' : q.concat(' 검색')}" />
<c:set var="nav" value="books" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<aside class="side pc-only" aria-label="도서 메뉴">
  <div class="side__group">
    <h2>둘러보기</h2>
    <nav class="side__nav">
      <a href="${ctx}/books" aria-current="${empty q and tab == 'bestseller' ? 'page' : 'false'}"><ui:icon name="flame" />베스트셀러</a>
      <a href="${ctx}/books?tab=new" aria-current="${empty q and tab == 'new' ? 'page' : 'false'}"><ui:icon name="sparkles" />신간</a>
      <a href="${ctx}/wishlist"><ui:icon name="heart" />찜한 책</a>
    </nav>
  </div>
  <c:if test="${not empty q}">
    <div class="side__group">
      <h2>정렬</h2>
      <nav class="side__nav">
        <c:forEach var="opt" items="accuracy,latest,sales">
          <c:url var="sideSortUrl" value="/books"><c:param name="q" value="${q}" /><c:param name="sort" value="${opt}" /></c:url>
          <a href="${sideSortUrl}" aria-current="${sort == opt}">${opt == 'accuracy' ? '정확도순' : (opt == 'latest' ? '최신순' : '전상욱 마켓 판매량순')}</a>
        </c:forEach>
      </nav>
    </div>
  </c:if>
  <c:if test="${not empty sessionScope.recent}">
    <div class="side__group">
      <h2>최근 본 책</h2>
      <ul class="side-recent">
        <c:forEach var="r" items="${sessionScope.recent}">
          <li><a href="${ctx}/books/detail?id=${fn:escapeXml(r.bookId)}">
            <span class="cover"><c:set var="coverBook" value="${r}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></span>
            <span><c:out value="${r.name}" /></span>
          </a></li>
        </c:forEach>
      </ul>
    </div>
  </c:if>
  <p class="side-note">3만 원 이상 주문하면 배송비가 무료예요.</p>
</aside>

<div class="shop-main">
<c:if test="${empty q and result.page == 1}">
  <section class="shop-hero pc-only" aria-labelledby="hero-title">
    <div>
      <h1 id="hero-title">${tab == 'new' ? '새로 나온 책을 만나 보세요' : '지금 많이 찾는 책이에요'}</h1>
      <p>위쪽 검색창에서 도서명·저자·출판사로 모든 책을 찾을 수 있어요.</p>
    </div>
    <div class="chips" aria-label="인기 검색어">
      <c:forEach var="kw" items="자바,파이썬,스프링,데이터베이스,알고리즘,웹 프로그래밍">
        <c:url var="kwUrl" value="/books"><c:param name="q" value="${kw}" /></c:url>
        <a class="chip" href="${kwUrl}"><ui:icon name="search" cls="icon-sm" />${kw}</a>
      </c:forEach>
    </div>
  </section>
</c:if>
<div class="page-head m-only">
  <h1 class="t-heading">어떤 책을 찾으세요?</h1>
</div>

<form class="search m-only" role="search" method="get" action="${ctx}/books">
  <ui:icon name="search" />
  <label class="sr-only" for="q">도서명, 저자, 출판사로 찾기</label>
  <input class="input" id="q" name="q" type="search" value="<c:out value='${q}'/>" placeholder="도서명, 저자, 출판사로 찾기" enterkeyhint="search">
</form>

<c:if test="${not apiOn}">
  <div class="banner banner--info" style="margin-top:12px"><ui:icon name="info" />
    <span>지금은 전상욱 마켓에 저장된 도서에서만 찾아요. <code>api.properties</code>에 카카오 REST API 키를 넣으면 모든 도서를 검색할 수 있어요.</span></div>
</c:if>
<c:if test="${not empty result.notice}">
  <div class="banner banner--warn" style="margin-top:12px" role="status"><ui:icon name="triangle-alert" /><span><c:out value="${result.notice}" /></span></div>
</c:if>

<%-- 최근 본 책 (세션) --%>
<c:if test="${empty q and not empty sessionScope.recent}">
  <section class="recent m-only" aria-labelledby="recent-title">
    <h2 class="t-title2" id="recent-title"><ui:icon name="history" cls="icon-sm" />최근 본 책</h2>
    <ul class="recent__list">
      <c:forEach var="r" items="${sessionScope.recent}">
        <li><a href="${ctx}/books/detail?id=${fn:escapeXml(r.bookId)}" class="recent__item">
          <span class="cover"><c:set var="coverBook" value="${r}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></span>
          <span class="recent__name"><c:out value="${r.name}" /></span>
        </a></li>
      </c:forEach>
    </ul>
  </section>
</c:if>

<c:choose>
  <c:when test="${empty q}">
    <div class="shop-head pc-only"><h2>${tab == 'new' ? '신간' : '베스트셀러'}</h2><span class="t-help">${tab == 'new' ? '출판일 순' : '전상욱 마켓 판매량 순'}</span></div>
    <nav class="tabs m-only" role="tablist" aria-label="도서 목록" style="margin-top:20px">
      <a role="tab" href="${ctx}/books" aria-selected="${tab == 'bestseller'}"><ui:icon name="flame" cls="icon-sm" />&nbsp;베스트셀러</a>
      <a role="tab" href="${ctx}/books?tab=new" aria-selected="${tab == 'new'}"><ui:icon name="sparkles" cls="icon-sm" />&nbsp;신간</a>
    </nav>
  </c:when>
  <c:otherwise>
    <div class="row-between" style="margin:20px 0 12px">
      <p class="t-help">
        "<strong style="color:var(--text-strong)"><c:out value="${q}" /></strong>" 검색 결과
        <strong class="num" style="color:var(--text-strong)"><fmt:formatNumber value="${result.total}" pattern="#,##0" /></strong>권
      </p>
      <a class="text-btn" href="${ctx}/books"><ui:icon name="x" cls="icon-sm" />검색 지우기</a>
    </div>
    <div class="chips m-only" aria-label="정렬" style="margin-bottom:16px">
      <c:forEach var="opt" items="accuracy,latest,sales">
        <c:url var="sortUrl" value="/books"><c:param name="q" value="${q}" /><c:param name="sort" value="${opt}" /></c:url>
        <a class="chip" href="${sortUrl}" aria-current="${sort == opt}">${opt == 'accuracy' ? '정확도순' : (opt == 'latest' ? '최신순' : '전상욱 마켓 판매량순')}</a>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${empty result.books}">
    <div class="card empty">
      <div class="empty__icon"><ui:icon name="search" /></div>
      <h2 class="t-title2">${empty q ? '아직 보여 줄 책이 없어요' : '찾는 책이 아직 없어요'}</h2>
      <p class="t-body3 muted">다른 낱말로 찾거나 띄어쓰기를 바꿔 보세요.</p>
      <a class="btn btn-sub" href="${ctx}/books">베스트셀러 보기</a>
    </div>
  </c:when>
  <c:otherwise>
    <ol class="book-grid" style="list-style:none;margin:0;padding:0" start="${(result.page - 1) * 12 + 1}">
      <c:forEach var="b" items="${result.books}" varStatus="st">
        <li>
          <a class="book-card" href="${ctx}/books/detail?id=${fn:escapeXml(b.bookId)}">
            <div class="cover"><c:set var="coverBook" value="${b}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></div>
            <div class="book-card__body">
              <div class="row" style="gap:6px">
                <c:if test="${empty q and tab == 'bestseller'}"><span class="rank num" aria-label="${(result.page - 1) * 12 + st.count}위">${(result.page - 1) * 12 + st.count}</span></c:if>
                <c:if test="${not empty b.category}"><ui:cat name="${b.category}" /></c:if>
              </div>
              <h2 class="t-title2 book-card__title"><c:out value="${b.name}" /></h2>
              <p class="meta"><span class="clip"><c:out value="${b.author}" /></span><span class="num"><c:out value="${b.releaseDate}" /></span></p>
              <p class="book-card__price"><span class="price"><fmt:formatNumber value="${b.unitPrice}" pattern="#,##0" /></span>원</p>
            </div>
          </a>
        </li>
      </c:forEach>
    </ol>

    <c:if test="${result.totalPages > 1}">
      <nav class="pager" aria-label="페이지">
        <c:url var="prevUrl" value="/books"><c:param name="tab" value="${tab}" /><c:if test="${not empty q}"><c:param name="q" value="${q}" /><c:param name="sort" value="${sort}" /></c:if><c:param name="page" value="${result.page - 1}" /></c:url>
        <c:url var="nextUrl" value="/books"><c:param name="tab" value="${tab}" /><c:if test="${not empty q}"><c:param name="q" value="${q}" /><c:param name="sort" value="${sort}" /></c:if><c:param name="page" value="${result.page + 1}" /></c:url>
        <c:choose>
          <c:when test="${result.hasPrev}"><a class="btn btn-sub btn-s" href="${prevUrl}"><ui:icon name="chevron-left" cls="icon-sm" />이전</a></c:when>
          <c:otherwise><button class="btn btn-sub btn-s" disabled><ui:icon name="chevron-left" cls="icon-sm" />이전</button></c:otherwise>
        </c:choose>
        <span class="t-help num"><strong style="color:var(--text-strong)">${result.page}</strong> / ${result.totalPages}</span>
        <c:choose>
          <c:when test="${result.hasNext}"><a class="btn btn-sub btn-s" href="${nextUrl}">다음<ui:icon name="chevron-right" cls="icon-sm" /></a></c:when>
          <c:otherwise><button class="btn btn-sub btn-s" disabled>다음<ui:icon name="chevron-right" cls="icon-sm" /></button></c:otherwise>
        </c:choose>
      </nav>
    </c:if>
    <c:if test="${result.source == 'KAKAO'}"><p class="t-help" style="margin-top:16px;text-align:center">검색 결과 제공: 카카오</p></c:if>
    <c:if test="${empty q}"><p class="t-help" style="margin-top:16px;text-align:center">${tab == 'new' ? '전상욱 마켓에 저장된 도서를 출판일 순으로 보여 줘요.' : '전상욱 마켓에서 실제로 팔린 권수 순서예요.'}</p></c:if>
  </c:otherwise>
</c:choose>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
