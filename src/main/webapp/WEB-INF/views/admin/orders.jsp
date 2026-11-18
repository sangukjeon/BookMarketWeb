<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="주문 관리" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="orders" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<div class="row-between" style="margin-bottom:12px;flex-wrap:wrap">
  <div class="chips" aria-label="상태">
    <a class="chip" href="${ctx}/admin/orders" aria-current="${empty status}">전체</a>
    <c:forEach var="sc" items="${statusCounts}">
      <a class="chip" href="${ctx}/admin/orders?status=${sc.key}" aria-current="${status == sc.key}">${sc.key == 'ORDERED' ? '주문완료' : (sc.key == 'PREPARING' ? '배송 준비 중' : (sc.key == 'SHIPPING' ? '배송 중' : (sc.key == 'DELIVERED' ? '배송 완료' : '취소됨')))} <span class="count">${sc.value}</span></a>
    </c:forEach>
  </div>
  <p class="t-help"><strong class="num" style="color:var(--text-strong)">${fn:length(orders)}</strong>건</p>
</div>
<div class="tbl-wrap">
  <table class="tbl tbl--wide tbl--tight">
    <caption class="sr-only">주문 목록</caption>
    <thead><tr><th scope="col">주문번호</th><th scope="col">주문한 분</th><th scope="col">상품</th><th scope="col" class="r">결제 금액</th><th scope="col">상태</th><th scope="col">주문 일시</th><th scope="col" class="r">관리</th></tr></thead>
    <tbody>
      <c:forEach var="o" items="${orders}">
        <tr>
          <td class="num nowrap"><a href="${ctx}/orders/receipt?id=${o.orderId}">${o.orderNo}</a></td>
          <td class="nowrap"><c:out value="${o.memberName}" /></td>
          <th scope="row" style="min-width:150px"><c:out value="${o.firstItemName}" /><c:if test="${o.itemCount > 1}"><span class="muted"> 외 ${o.itemCount - 1}종</span></c:if></th>
          <td class="r nowrap"><fmt:formatNumber value="${o.payAmount}" pattern="#,##0" />원<c:if test="${not empty o.couponCode}"><br><span class="t-help"><c:out value="${o.couponCode}" /></span></c:if></td>
          <td class="nowrap"><span class="pill ${o.statusPill}">${o.statusLabel}</span></td>
          <td class="num nowrap">${o.orderedAtText}<c:if test="${not empty o.shippedAtText}"><br><span class="t-help">배송 시작 ${o.shippedAtText}</span></c:if></td>
          <td class="r nowrap">
            <div class="order-actions">
              <c:if test="${not empty o.nextStatus}">
                <form method="post" action="${ctx}/admin/orders/status"<c:if test="${o.nextStatus == 'SHIPPING'}"> data-confirm="배송을 시작할까요?" data-confirm-desc="${o.orderNo} — 배송이 시작되면 회원이 주문을 취소할 수 없어요." data-confirm-ok="배송 시작하기"</c:if>>
                  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                  <input type="hidden" name="id" value="${o.orderId}">
                  <input type="hidden" name="from" value="${o.status}">
                  <input type="hidden" name="next" value="/admin/orders${empty status ? '' : '?status='.concat(status)}">
                  <button class="btn btn-tint btn-s" type="submit"><ui:icon name="${o.nextStatus == 'PREPARING' ? 'package' : (o.nextStatus == 'SHIPPING' ? 'truck' : 'circle-check')}" cls="icon-sm" />${o.nextActionLabel}</button>
                </form>
              </c:if>
              <c:if test="${o.cancellable}">
                <form method="post" action="${ctx}/orders/cancel" data-confirm="이 주문을 취소할까요?" data-confirm-desc="${o.orderNo} — 취소하면 되돌릴 수 없어요." data-confirm-ok="주문 취소하기">
                  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                  <input type="hidden" name="id" value="${o.orderId}">
                  <input type="hidden" name="next" value="/admin/orders${empty status ? '' : '?status='.concat(status)}">
                  <button class="btn btn-sub btn-s" type="submit" style="color:var(--danger-strong)">취소하기</button>
                </form>
              </c:if>
            </div>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty orders}"><tr class="empty-row"><td colspan="7">해당하는 주문이 없어요</td></tr></c:if>
    </tbody>
  </table>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
