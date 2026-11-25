<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="찜한 책" />
<c:set var="nav" value="wishlist" />
<c:set var="containerCls" value="container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head"><h1 class="t-heading">찜한 책</h1></div>
<c:choose>
  <c:when test="${empty books}">
    <div class="card empty">
      <img class="empty__mascot" src="${ctx}/images/mascot/bear-search.png" alt="" width="132" height="126">
      <h2 class="t-title2">아직 찜한 책이 없어요</h2>
      <p class="t-body3 muted">책 상세 화면에서 하트를 누르면 여기에 모여요.</p>
      <a class="btn btn-sub" href="${ctx}/books">도서 둘러보기</a>
    </div>
  </c:when>
  <c:otherwise>
    <p class="t-help" style="margin-bottom:12px">찜한 책 <strong class="num" style="color:var(--text-strong)">${fn:length(books)}</strong>권</p>
    <ul class="book-grid" style="list-style:none;margin:0;padding:0">
      <c:forEach var="b" items="${books}">
        <li class="wish-item">
          <a class="book-card" href="${ctx}/books/detail?id=${fn:escapeXml(b.bookId)}">
            <div class="cover"><c:set var="coverBook" value="${b}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></div>
            <div class="book-card__body">
              <h2 class="t-title2 book-card__title" style="padding-right:36px"><c:out value="${b.name}" /></h2>
              <p class="meta"><span class="clip"><c:out value="${b.author}" /></span></p>
              <p class="book-card__price"><span class="price"><fmt:formatNumber value="${b.unitPrice}" pattern="#,##0" /></span>원</p>
            </div>
          </a>
          <form class="wish-item__actions" method="post" action="${ctx}/wishlist/toggle">
            <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
            <input type="hidden" name="id" value="<c:out value='${b.bookId}'/>">
            <input type="hidden" name="next" value="/wishlist">
            <button class="icon-btn heart is-on" type="submit" aria-pressed="true" aria-label="<c:out value='${b.name}'/> 찜 풀기"><ui:icon name="heart" /></button>
          </form>
          <form class="wish-item__cart" method="post" action="${ctx}/cart/add">
            <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
            <input type="hidden" name="id" value="<c:out value='${b.bookId}'/>">
            <input type="hidden" name="next" value="/wishlist">
            <button class="btn btn-sub btn-s" type="submit"><ui:icon name="shopping-cart" cls="icon-sm" />담기</button>
          </form>
        </li>
      </c:forEach>
    </ul>
  </c:otherwise>
</c:choose>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
