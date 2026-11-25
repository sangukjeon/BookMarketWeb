<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="장바구니" />
<c:set var="nav" value="cart" />
<c:set var="cart" value="${sessionScope.cart}" />
<c:set var="plate" value="${not cart.isEmpty()}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="page-head row-between">
  <h1 class="t-heading">장바구니</h1>
  <c:if test="${not cart.isEmpty()}">
    <form method="post" action="${ctx}/cart/clear" data-confirm="장바구니를 비울까요?" data-confirm-desc="담긴 책이 모두 빠져요." data-confirm-ok="비우기">
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <button class="text-btn" type="submit" style="color:var(--text-2)">전체 비우기</button>
    </form>
  </c:if>
</div>

<c:choose>
  <c:when test="${cart.isEmpty()}">
    <div class="card empty">
      <img class="empty__mascot" src="${ctx}/images/mascot/bear-search.png" alt="" width="132" height="126">
      <h2 class="t-title2">장바구니가 비어 있어요</h2>
      <p class="t-body3 muted">마음에 드는 책을 담아 보세요.</p>
      <a class="btn btn-sub" href="${ctx}/books">도서 둘러보기</a>
    </div>
  </c:when>
  <c:otherwise>
    <c:set var="sum" value="${cart.itemTotal}" />
    <c:set var="ship" value="${cart.shippingFeeFor(sum)}" />
    <div class="split">
      <section class="card card--flush" aria-label="담은 책">
        <div class="cart-table-head pc-only" aria-hidden="true"><span></span><span>상품 정보</span><span>수량</span><span>금액</span><span></span></div>
        <c:forEach var="it" items="${cart.items}">
          <div class="cart-item">
            <a class="cover cover--sm" href="${ctx}/books/detail?id=${it.bookID}" tabindex="-1" aria-hidden="true"><c:set var="coverBook" value="${it.itemBook}" /><%@ include file="/WEB-INF/views/common/cover.jspf" %></a>
            <div>
              <div class="cart-item__top">
                <div>
                  <h2 class="t-title2"><c:out value="${it.itemBook.name}" /></h2>
                  <p class="t-help num"><fmt:formatNumber value="${it.itemBook.unitPrice}" pattern="#,##0" />원</p>
                  <c:choose>
                    <c:when test="${it.itemBook.soldOut}"><p class="help help--error" role="alert">품절됐어요. 빼고 주문해 주세요</p></c:when>
                    <c:when test="${it.quantity > it.itemBook.stock}"><p class="help help--error" role="alert">재고가 ${it.itemBook.stock}권뿐이에요. 수량을 줄여 주세요</p></c:when>
                    <c:when test="${it.itemBook.lowStock}"><p class="help" style="color:var(--warn-strong)">${it.itemBook.stock}권 남았어요</p></c:when>
                  </c:choose>
                </div>
                <form method="post" action="${ctx}/cart/remove">
                  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                  <input type="hidden" name="id" value="${it.bookID}">
                  <button class="icon-btn" type="submit" aria-label="<c:out value='${it.itemBook.name}'/> 빼기"><ui:icon name="x" /></button>
                </form>
              </div>
              <div class="cart-item__bottom">
                <div class="stepper" role="group" aria-label="<c:out value='${it.itemBook.name}'/> 수량">
                  <form method="post" action="${ctx}/cart/decrease">
                    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                    <input type="hidden" name="id" value="${it.bookID}">
                    <button type="submit" aria-label="수량 줄이기" ${it.quantity <= 1 ? 'disabled' : ''}><ui:icon name="minus" cls="icon-sm" /></button>
                  </form>
                  <output aria-live="polite">${it.quantity}</output>
                  <form method="post" action="${ctx}/cart/increase">
                    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                    <input type="hidden" name="id" value="${it.bookID}">
                    <button type="submit" aria-label="수량 늘리기" ${it.quantity >= 99 or it.quantity >= it.itemBook.stock ? 'disabled' : ''}><ui:icon name="plus" cls="icon-sm" /></button>
                  </form>
                </div>
                <p class="price" style="color:var(--text-strong)"><fmt:formatNumber value="${it.totalPrice}" pattern="#,##0" /><small>원</small></p>
              </div>
            </div>
          </div>
        </c:forEach>
      </section>

      <aside class="aside">
        <div class="card">
          <h2 class="t-title2 card-title">결제 예정 금액</h2>
          <dl class="summary">
            <div><dt>상품 금액</dt><dd><fmt:formatNumber value="${sum}" pattern="#,##0" />원</dd></div>
            <div><dt>배송비</dt><dd>${ship == 0 ? '무료' : ''}<c:if test="${ship > 0}"><fmt:formatNumber value="${ship}" pattern="#,##0" />원</c:if></dd></div>
            <div class="total"><dt>합계</dt><dd><fmt:formatNumber value="${sum + ship}" pattern="#,##0" />원</dd></div>
          </dl>
          <c:if test="${ship > 0}">
            <div class="banner banner--info" style="margin-top:16px"><ui:icon name="truck" />
              <span><fmt:formatNumber value="${30000 - sum}" pattern="#,##0" />원어치 더 담으면 배송비가 무료예요.</span></div>
          </c:if>
          <p class="t-help" style="margin-top:12px">쿠폰은 다음 단계인 주문서에서 쓸 수 있어요.<c:if test="${not empty me}"> 장바구니는 저장돼서 다른 PC에서도 그대로 보여요.</c:if></p>
        </div>
        <div class="plate">
          <div class="plate__inner">
            <c:choose>
              <c:when test="${stockProblem}"><button class="btn btn-main btn-soldout" type="button" disabled>재고를 확인해 주세요</button></c:when>
              <c:otherwise><a class="btn btn-main" href="${ctx}/order"><span class="num"><fmt:formatNumber value="${sum + ship}" pattern="#,##0" />원</span> 주문하기</a></c:otherwise>
            </c:choose>
          </div>
        </div>
      </aside>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
