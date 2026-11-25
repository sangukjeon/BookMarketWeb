<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="영수증" />
<c:set var="nav" value="orders" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<a class="text-btn no-print" href="${ctx}/orders" style="margin:-8px 0 8px -4px"><ui:icon name="chevron-left" />주문 내역</a>

<article class="card" aria-labelledby="r-title">
  <div class="receipt-head">
    <c:choose>
      <c:when test="${order.cancelled}">
        <div class="done done--cancel"><ui:icon name="circle-x" /></div>
        <h1 class="t-heading" id="r-title">취소된 주문이에요</h1>
      </c:when>
      <c:otherwise>
        <img class="receipt__mascot no-print" src="${ctx}/images/mascot/bear-cheer.png" alt="" width="120" height="105">
        <h1 class="t-heading" id="r-title">주문을 마쳤어요</h1>
      </c:otherwise>
    </c:choose>
    <p class="t-body3 muted num" style="margin-top:4px">주문번호 ${order.orderNo}</p>
    <p style="margin-top:12px">
      <c:choose>
        <c:when test="${order.cancelled}"><span class="pill pill--danger">취소됨</span></c:when>
        <c:otherwise><span class="pill ${order.statusPill}">${order.statusLabel}</span></c:otherwise>
      </c:choose>
    </p>
  </div>

  <c:if test="${not order.cancelled}">
    <ol class="ship-steps" aria-label="배송 진행 상황: ${order.statusLabel}">
      <li class="${order.step >= 1 ? 'is-done' : ''}${order.step == 1 ? ' is-now' : ''}"><span class="ship-steps__dot"><ui:icon name="receipt" cls="icon-sm" /></span><span class="ship-steps__label">주문완료</span></li>
      <li class="${order.step >= 2 ? 'is-done' : ''}${order.step == 2 ? ' is-now' : ''}"><span class="ship-steps__dot"><ui:icon name="package" cls="icon-sm" /></span><span class="ship-steps__label">배송 준비</span></li>
      <li class="${order.step >= 3 ? 'is-done' : ''}${order.step == 3 ? ' is-now' : ''}"><span class="ship-steps__dot"><ui:icon name="truck" cls="icon-sm" /></span><span class="ship-steps__label">배송 중</span></li>
      <li class="${order.step >= 4 ? 'is-done' : ''}${order.step == 4 ? ' is-now' : ''}"><span class="ship-steps__dot"><ui:icon name="house" cls="icon-sm" /></span><span class="ship-steps__label">배송 완료</span></li>
    </ol>
  </c:if>

  <h2 class="t-title2 section-title" style="margin-top:8px">배송 정보</h2>
  <dl class="kv">
    <dt>받는 분</dt><dd><c:out value="${order.receiverName}" /></dd>
    <dt>연락처</dt><dd class="num"><c:out value="${order.receiverPhone}" /></dd>
    <dt>배송지</dt><dd><c:out value="${order.address}" /></dd>
    <dt>주문 일시</dt><dd class="num">${order.orderedAtText}</dd>
    <c:if test="${not empty order.shippedAtText}"><dt>배송 시작</dt><dd class="num">${order.shippedAtText}</dd></c:if>
    <c:if test="${not empty order.deliveredAtText}"><dt>배송 완료</dt><dd class="num">${order.deliveredAtText}</dd></c:if>
    <c:if test="${order.cancelled}"><dt>취소 일시</dt><dd class="num">${order.cancelledAtText}</dd></c:if>
  </dl>

  <h2 class="t-title2 section-title">주문한 책</h2>
  <div class="tbl-wrap">
    <table class="tbl">
      <caption class="sr-only">주문한 책 목록</caption>
      <thead><tr><th scope="col">도서</th><th scope="col" class="r">단가</th><th scope="col" class="r">수량</th><th scope="col" class="r">금액</th></tr></thead>
      <tbody>
        <c:forEach var="it" items="${order.items}">
          <tr>
            <th scope="row"><c:out value="${it.bookName}" /><br><span class="t-help num"><c:out value="${it.bookId}" /></span></th>
            <td class="r nowrap"><fmt:formatNumber value="${it.unitPrice}" pattern="#,##0" />원</td>
            <td class="r">${it.quantity}</td>
            <td class="r nowrap"><fmt:formatNumber value="${it.lineTotal}" pattern="#,##0" />원</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <dl class="summary" style="margin-top:20px">
    <div><dt>상품 금액</dt><dd><fmt:formatNumber value="${order.itemTotal}" pattern="#,##0" />원</dd></div>
    <div class="minus"><dt>쿠폰 할인<c:if test="${not empty order.couponCode}"> <span class="side-pill">${order.couponCode}</span></c:if></dt><dd>${order.discount > 0 ? '−' : ''}<fmt:formatNumber value="${order.discount}" pattern="#,##0" />원</dd></div>
    <div><dt>배송비</dt><dd><c:choose><c:when test="${order.shippingFee == 0}">무료</c:when><c:otherwise><fmt:formatNumber value="${order.shippingFee}" pattern="#,##0" />원</c:otherwise></c:choose></dd></div>
    <div class="total"><dt>${order.cancelled ? '취소한 금액' : '결제한 금액'}</dt><dd><fmt:formatNumber value="${order.payAmount}" pattern="#,##0" />원</dd></div>
  </dl>
</article>

<div class="row no-print" style="margin-top:16px;flex-wrap:wrap">
  <button class="btn btn-sub" type="button" onclick="window.print()"><ui:icon name="printer" />인쇄하기</button>
  <a class="btn btn-tint" href="${ctx}/books" style="flex:1">쇼핑 계속하기</a>
</div>
<c:if test="${not order.cancelled and not order.cancellable}">
  <p class="notice-box no-print" style="margin-top:8px"><ui:icon name="truck" cls="icon-sm" /> 배송이 시작돼서 취소할 수 없어요. 받은 뒤 문제가 있으면 고객센터로 문의해 주세요.</p>
</c:if>
<c:if test="${order.cancellable}">
  <form class="no-print" method="post" action="${ctx}/orders/cancel" style="margin-top:8px"
        data-confirm="이 주문을 취소할까요?" data-confirm-desc="취소하면 되돌릴 수 없어요. 쓴 쿠폰은 다시 쓸 수 있게 돼요." data-confirm-ok="주문 취소하기">
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <input type="hidden" name="id" value="${order.orderId}">
    <button class="btn btn-sub btn-block" type="submit" style="color:var(--danger-strong)"><ui:icon name="ban" />주문 취소하기</button>
  </form>
</c:if>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
