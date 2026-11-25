<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="${book.name}" />
<c:set var="nav" value="books" />
<c:set var="plate" value="${true}" />
<c:url var="selfUrl" value="/books/detail"><c:param name="id" value="${book.bookId}" /></c:url>
<c:set var="selfPath" value="/books/detail?id=${fn:escapeXml(book.bookId)}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<a class="text-btn" href="${ctx}/books" style="margin:-8px 0 8px -4px"><ui:icon name="chevron-left" />도서 목록</a>

<article class="detail">
  <div class="cover cover--lg"><c:set var="coverBook" value="${book}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></div>

  <div class="detail__info">
    <div class="row-between" style="align-items:flex-start">
      <div>
        <c:if test="${not empty book.genre}"><a href="${ctx}/books?genre=${book.genre.slug}" style="text-decoration:none"><ui:cat name="${book.category}" /></a></c:if>
        <h1 class="t-heading" style="margin:8px 0 4px"><c:out value="${book.name}" /></h1>
        <p class="t-body3 muted"><c:out value="${book.author}" /><c:if test="${not empty book.publisher}"> · <c:out value="${book.publisher}" /></c:if></p>
      </div>
      <form method="post" action="${ctx}/wishlist/toggle">
        <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
        <input type="hidden" name="id" value="<c:out value='${book.bookId}'/>">
        <input type="hidden" name="next" value="${selfPath}">
        <button class="icon-btn heart ${wished ? 'is-on' : ''}" type="submit" aria-pressed="${wished ? 'true' : 'false'}" aria-label="${wished ? '찜 풀기' : '찜하기'}">
          <ui:icon name="heart" cls="icon-lg" />
        </button>
      </form>
    </div>

    <p class="m-only" style="margin:16px 0 4px;font-size:24px;line-height:32px;color:var(--text-strong)"><span class="price"><fmt:formatNumber value="${book.unitPrice}" pattern="#,##0" /></span>원</p>
    <a class="rating-line" href="#reviews">
      <c:set var="starValue" value="${avgRating}" /><%@ include file="/WEB-INF/views/common/stars.jspf" %>
      <span class="num">${fn:length(reviews) == 0 ? '아직 리뷰가 없어요' : avgRating}</span>
      <c:if test="${fn:length(reviews) > 0}"><span class="muted">리뷰 ${fn:length(reviews)}개</span></c:if>
    </a>

    <div class="card" style="margin-top:20px">
      <h2 class="t-title2 card-title">책 소개</h2>
      <p class="t-body1"><c:out value="${empty book.description ? '소개 글이 아직 없어요.' : book.description}" /></p>
    </div>
    <div class="card" style="margin-top:12px">
      <h2 class="sr-only">도서 정보</h2>
      <dl>
        <dt>ISBN</dt><dd class="num"><c:out value="${book.bookId}" /></dd>
        <c:if test="${not empty book.publisher}"><dt>출판사</dt><dd><c:out value="${book.publisher}" /></dd></c:if>
        <dt>장르</dt><dd><c:choose><c:when test="${not empty book.genre}"><a href="${ctx}/books?genre=${book.genre.slug}"><c:out value="${book.category}" /></a></c:when><c:otherwise>-</c:otherwise></c:choose></dd>
        <dt>재고</dt><dd><c:choose><c:when test="${book.soldOut}"><span class="pill pill--danger">품절</span></c:when><c:when test="${book.lowStock}"><span class="pill pill--warn">${book.stock}권 남았어요</span></c:when><c:otherwise>${book.stock}권</c:otherwise></c:choose></dd>
        <dt>출판일</dt><dd class="num"><c:out value="${book.releaseDate}" /></dd>
        <dt>배송</dt><dd>3만 원 이상 주문하면 배송비가 무료예요</dd>
      </dl>
    </div>

    <section class="card" id="reviews" style="margin-top:12px" aria-labelledby="rv-title">
      <div class="row-between card-title">
        <h2 class="t-title2" id="rv-title">리뷰 <span class="muted num">${fn:length(reviews)}</span></h2>
      </div>

      <c:choose>
        <c:when test="${canReview}">
          <form class="review-form" method="post" action="${ctx}/books/review">
            <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
            <input type="hidden" name="id" value="<c:out value='${book.bookId}'/>">
            <fieldset class="star-field">
              <legend class="t-label">별점을 골라 주세요</legend>
              <div class="star-field__row">
                <div class="star-input">
                  <c:forEach begin="1" end="5" var="n">
                    <c:set var="v" value="${6 - n}" />
                    <input type="radio" id="star${v}" name="rating" value="${v}" class="star-input__radio" ${v == 5 ? 'checked' : ''} required>
                    <label for="star${v}" title="${v}점"><svg class="icon" aria-hidden="true"><use href="${ctx}/images/icons.svg#i-star"/></svg><span class="sr-only">${v}점</span></label>
                  </c:forEach>
                </div>
                <output class="star-field__score num" for="star1 star2 star3 star4 star5" aria-live="polite">5점</output>
              </div>
            </fieldset>
            <div class="field" style="margin-top:8px">
              <label class="sr-only" for="content">리뷰 내용</label>
              <textarea class="textarea" id="content" name="content" maxlength="300" placeholder="이 책은 어땠나요? (300자까지)" required style="min-height:88px"></textarea>
            </div>
            <div class="row" style="justify-content:flex-end;margin-top:8px"><button class="btn btn-tint btn-s" type="submit">리뷰 남기기</button></div>
          </form>
        </c:when>
        <c:when test="${empty me}">
          <p class="notice-box">책을 구매한 회원이 리뷰를 남길 수 있어요. <a href="${ctx}/login?next=${fn:escapeXml(selfPath)}">로그인하기</a></p>
        </c:when>
        <c:when test="${not reviewed}">
          <p class="notice-box">이 책을 주문하면 리뷰를 남길 수 있어요.</p>
        </c:when>
      </c:choose>

      <c:choose>
        <c:when test="${empty reviews}">
          <p class="t-body3 muted" style="text-align:center;padding:20px 0 4px">첫 리뷰를 기다리고 있어요</p>
        </c:when>
        <c:otherwise>
          <ul class="review-list">
            <c:forEach var="r" items="${reviews}">
              <li>
                <div class="row-between">
                  <div class="row"><c:set var="starValue" value="${r.rating}" /><%@ include file="/WEB-INF/views/common/stars.jspf" %><span class="t-label"><c:out value="${r.maskedName}" /></span></div>
                  <div class="row">
                    <span class="t-help num">${r.createdAtText}</span>
                    <c:if test="${not empty me and (me.memberNo == r.memberNo or me.admin)}">
                      <form method="post" action="${ctx}/books/review/delete" data-confirm="리뷰를 지울까요?" data-confirm-ok="지우기">
                        <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                        <input type="hidden" name="id" value="<c:out value='${book.bookId}'/>">
                        <input type="hidden" name="reviewId" value="${r.reviewId}">
                        <button class="text-btn" type="submit" style="color:var(--text-2);font-size:13px">지우기</button>
                      </form>
                    </c:if>
                  </div>
                </div>
                <p class="t-body3" style="margin-top:4px"><c:out value="${r.content}" /></p>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <c:if test="${not empty sameGenre}">
      <section class="same-genre" aria-labelledby="same-title">
        <div class="row-between" style="margin-bottom:12px">
          <h2 class="t-title2" id="same-title">같은 장르의 다른 책</h2>
          <a class="text-btn" href="${ctx}/books?genre=${book.genre.slug}">${book.category} 더 보기<ui:icon name="chevron-right" cls="icon-sm" /></a>
        </div>
        <ul class="same-genre__list">
          <c:forEach var="sb" items="${sameGenre}">
            <li><a href="${ctx}/books/detail?id=${fn:escapeXml(sb.bookId)}">
              <span class="cover"><c:set var="coverBook" value="${sb}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></span>
              <span class="same-genre__name"><c:out value="${sb.name}" /></span>
              <span class="t-help num"><fmt:formatNumber value="${sb.unitPrice}" pattern="#,##0" />원</span>
            </a></li>
          </c:forEach>
        </ul>
      </section>
    </c:if>
  </div>
  <aside class="buybox" aria-label="구매">
    <div class="card pc-only">
      <p class="t-help">판매가</p>
      <p class="buybox__price"><fmt:formatNumber value="${book.unitPrice}" pattern="#,##0" /><small>원</small></p>
      <dl class="buybox__rows">
        <div><dt>배송비</dt><dd>${book.unitPrice >= 30000 ? '무료' : '3,000원 (3만 원 이상 무료)'}</dd></div>
        <div><dt>쿠폰</dt><dd>쿠폰함 쿠폰을 주문서에서</dd></div>
        <div><dt>재고</dt><dd>${book.soldOut ? '품절' : (book.lowStock ? book.stock.toString().concat('권 남았어요') : '있어요')}</dd></div>
        <div><dt>리뷰</dt><dd><c:choose><c:when test="${empty reviews}">아직 없어요</c:when><c:otherwise><span class="num" style="display:inline-flex;align-items:center;gap:2px"><ui:icon name="star" cls="icon-sm star-on" />${avgRating}</span> · ${fn:length(reviews)}개</c:otherwise></c:choose></dd></div>
      </dl>
      <form class="plate" method="post" action="${ctx}/cart/add">
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <input type="hidden" name="id" value="<c:out value='${book.bookId}'/>">
      <input type="hidden" name="next" value="${selfPath}">
      <div class="plate__inner">
        <c:choose>
          <c:when test="${book.soldOut}"><button class="btn btn-main btn-soldout" type="button" disabled>품절이에요</button></c:when>
          <c:otherwise>
        <button class="btn btn-sub" type="submit" name="then" value="stay" aria-label="장바구니에 담기"><ui:icon name="shopping-cart" />담기</button>
        <button class="btn btn-main" type="submit" name="then" value="buy">바로 구매하기</button>
          </c:otherwise>
        </c:choose>
      </div>
    </form>
    </div>
    <div class="m-only">
    <form class="plate" method="post" action="${ctx}/cart/add">
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <input type="hidden" name="id" value="<c:out value='${book.bookId}'/>">
      <input type="hidden" name="next" value="${selfPath}">
      <div class="plate__inner">
        <c:choose>
          <c:when test="${book.soldOut}"><button class="btn btn-main btn-soldout" type="button" disabled>품절이에요</button></c:when>
          <c:otherwise>
        <button class="btn btn-sub" type="submit" name="then" value="stay" aria-label="장바구니에 담기"><ui:icon name="shopping-cart" />담기</button>
        <button class="btn btn-main" type="submit" name="then" value="buy">바로 구매하기</button>
          </c:otherwise>
        </c:choose>
      </div>
    </form>
    </div>
  </aside>
</article>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
