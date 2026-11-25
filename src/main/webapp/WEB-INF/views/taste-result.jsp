<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="${bear.label}" />
<c:set var="containerCls" value="container--read" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<section class="card taste-result" aria-labelledby="bear-title">
  <p class="eyebrow-s">나와 닮은 곰은</p>
  <img class="taste-result__bear" src="${ctx}/images/mascot/${bear.image}" alt="" width="160" height="160">
  <h1 class="t-heading" id="bear-title">${bear.label}</h1>
  <p class="t-body2">${bear.description}</p>
  <div class="chips" style="justify-content:center;margin-top:16px" aria-label="어울리는 장르">
    <c:forEach var="g" items="${bear.genres}">
      <a class="chip" href="${ctx}/books?genre=${g.slug}"><span class="genre-dot genre-dot--${g.tone}" aria-hidden="true"></span>${g.label}</a>
    </c:forEach>
  </div>
  <c:choose>
    <c:when test="${empty me}"><p class="banner banner--info" style="margin-top:16px;text-align:left"><ui:icon name="info" /><span>로그인하고 다시 하면 결과가 저장되고 500원 쿠폰도 받아요. <a href="${ctx}/login?next=/taste">로그인하기</a></span></p></c:when>
    <c:when test="${rewarded}"><p class="t-help" style="margin-top:16px">결과를 마이페이지에 저장했어요. 테스트 쿠폰(500원)은 <a href="${ctx}/coupons">쿠폰함</a>에 있어요.</p></c:when>
  </c:choose>
</section>

<c:if test="${not empty picks}">
  <section class="same-genre" aria-labelledby="picks-title">
    <h2 class="t-title2" id="picks-title" style="margin-bottom:12px">${bear.label}에게 추천하는 책</h2>
    <ul class="same-genre__list same-genre__list--3">
      <c:forEach var="sb" items="${picks}">
        <li><a href="${ctx}/books/detail?id=${fn:escapeXml(sb.bookId)}">
          <span class="cover"><c:set var="coverBook" value="${sb}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></span>
          <span class="same-genre__name"><c:out value="${sb.name}" /></span>
          <span class="t-help num"><fmt:formatNumber value="${sb.unitPrice}" pattern="#,##0" />원</span>
        </a></li>
      </c:forEach>
    </ul>
  </section>
</c:if>
<p style="margin-top:24px;text-align:center"><a class="btn btn-sub" href="${ctx}/taste">다시 해 보기</a></p>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
