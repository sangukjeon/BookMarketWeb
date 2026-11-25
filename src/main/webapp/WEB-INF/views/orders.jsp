<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="주문 내역" />
<c:set var="nav" value="orders" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head"><h1 class="t-heading">주문 내역</h1></div>
<c:choose>
  <c:when test="${empty orders}">
    <div class="card empty">
      <img class="empty__mascot" src="${ctx}/images/mascot/bear.png" alt="" width="132" height="120">
      <h2 class="t-title2">아직 주문한 책이 없어요</h2>
      <p class="t-body3 muted">주문하면 여기에서 영수증을 다시 볼 수 있어요.</p>
      <a class="btn btn-sub" href="${ctx}/books">도서 둘러보기</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="tbl-wrap pc-only">
      <table class="tbl tbl--wide orders-table">
        <caption class="sr-only">주문 내역</caption>
        <thead><tr><th scope="col">주문 일시</th><th scope="col">주문번호</th><th scope="col">상품</th><th scope="col" class="r">결제 금액</th><th scope="col">상태</th><th scope="col" class="r">영수증</th></tr></thead>
        <tbody>
          <c:forEach var="o" items="${orders}">
            <tr>
              <td class="num nowrap">${o.orderedAtText}</td>
              <td class="num nowrap">${o.orderNo}</td>
              <th scope="row"><c:out value="${o.firstItemName}" /><c:if test="${o.itemCount > 1}"><span class="muted"> 외 ${o.itemCount - 1}종</span></c:if></th>
              <td class="r nowrap" style="${o.cancelled ? 'color:var(--text-3);text-decoration:line-through' : ''}"><fmt:formatNumber value="${o.payAmount}" pattern="#,##0" />원</td>
              <td class="nowrap"><span class="pill ${o.statusPill}">${o.statusLabel}</span></td>
              <td class="r"><a class="btn btn-sub btn-s" href="${ctx}/orders/receipt?id=${o.orderId}">보기</a></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <div class="card card--flush m-only">
      <c:forEach var="o" items="${orders}">
        <a class="list-row" href="${ctx}/orders/receipt?id=${o.orderId}">
          <div class="list-row__body">
            <p class="t-title2">
              <c:out value="${o.firstItemName}" /><c:if test="${o.itemCount > 1}"> 외 ${o.itemCount - 1}종</c:if>
            </p>
            <p class="meta"><span class="num">${o.orderedAtText}</span><span class="num">${o.orderNo}</span></p>
          </div>
          <span class="pill ${o.statusPill}">${o.statusLabel}</span>
          <p class="price" style="color:${o.cancelled ? 'var(--text-3)' : 'var(--text-strong)'};${o.cancelled ? 'text-decoration:line-through' : ''}"><fmt:formatNumber value="${o.payAmount}" pattern="#,##0" />원</p>
          <ui:icon name="chevron-right" cls="chev" />
        </a>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
