<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="주문서" />
<c:set var="nav" value="cart" />
<c:set var="plate" value="${true}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<a class="text-btn" href="${ctx}/cart" style="margin:-8px 0 8px -4px"><ui:icon name="chevron-left" />장바구니</a>
<div class="page-head"><h1 class="t-heading">주문서</h1></div>

<form method="post" action="${ctx}/order" novalidate>
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <c:if test="${not empty error}">
    <div class="banner banner--danger" role="alert" style="margin-bottom:12px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
  </c:if>
  <div class="split">
    <div class="stack">
      <section class="card" data-shipping aria-labelledby="ship-title">
        <h2 class="t-title2 card-title" id="ship-title">배송 정보</h2>
        <p class="t-body3 muted" style="margin:-4px 0 12px">받는 분이 회원 정보와 같나요?</p>
        <div class="segment" style="margin-bottom:16px">
          <button type="button" class="chip" data-fill="me" aria-pressed="true">네, 같아요</button>
          <button type="button" class="chip" data-fill="new" aria-pressed="false">새로 입력할게요</button>
        </div>
        <div class="field-grid field-grid--2">
          <div class="field">
            <label for="receiverName">받는 분</label>
            <input class="input" id="receiverName" name="receiverName" value="<c:out value='${receiverName}'/>" data-src="<c:out value='${me.name}'/>" autocomplete="name" required>
          </div>
          <div class="field">
            <label for="receiverPhone">연락처</label>
            <input class="input" id="receiverPhone" name="receiverPhone" value="<c:out value='${receiverPhone}'/>" data-src="<c:out value='${me.phone}'/>" inputmode="tel" autocomplete="tel" placeholder="010-1234-5678" required>
          </div>
        </div>
        <div class="field" style="margin-top:16px">
          <label for="address">배송지</label>
          <input class="input" id="address" name="address" value="<c:out value='${address}'/>" data-src="<c:out value='${me.address}'/>" autocomplete="street-address" placeholder="예: 서울시 종로구 세종대로 1, 101동 1001호" required>
        </div>
        <label class="check" style="margin-top:4px"><input type="checkbox" name="saveAddress" ${saveAddress ? 'checked' : ''}>이 배송지를 기본 배송지로 저장하기</label>
      </section>

      <section class="card" aria-labelledby="coupon-title">
        <h2 class="t-title2 card-title" id="coupon-title">쿠폰</h2>
        <div class="field">
          <label class="sr-only" for="couponCode">쿠폰 번호</label>
          <div class="input-row">
            <input class="input" id="couponCode" name="couponCode" value="<c:out value='${couponCode}'/>" placeholder="쿠폰 번호 입력" autocapitalize="characters" ${not empty couponError ? 'aria-invalid="true"' : ''} aria-describedby="couponHelp">
            <button class="btn btn-sub btn-s" type="submit" name="action" value="apply" formnovalidate>적용하기</button>
          </div>
          <c:choose>
            <c:when test="${not empty couponError}"><span class="help help--error" id="couponHelp" role="alert"><c:out value="${couponError}" /></span></c:when>
            <c:when test="${not empty coupon}"><span class="help" id="couponHelp" style="color:var(--success-strong)"><c:out value="${coupon.name}" /> 쿠폰을 적용했어요.</span></c:when>
            <c:otherwise><span class="help" id="couponHelp">쿠폰은 회원마다 한 번씩 쓸 수 있어요. 아래 쿠폰을 눌러도 바로 들어가요.</span></c:otherwise>
          </c:choose>
        </div>
        <c:if test="${not empty coupons}">
          <div class="chips" style="margin-top:12px">
            <c:forEach var="cp" items="${coupons}">
              <c:set var="isUsed" value="${usedCoupons.contains(cp.code)}" />
              <button class="chip" type="submit" name="couponPick" value="${cp.code}" formnovalidate aria-pressed="${cp.code == coupon.code}" ${isUsed ? 'disabled' : ''}>
                <ui:icon name="ticket-percent" cls="icon-sm" /><c:out value="${cp.code}" /><span class="count">${isUsed ? '사용함' : cp.benefitLabel}</span>
              </button>
            </c:forEach>
          </div>
        </c:if>
      </section>

      <section class="card card--flush" aria-labelledby="items-title">
        <h2 class="t-title2" id="items-title" style="padding:16px 16px 4px">주문할 책 <span class="muted num">${fn:length(summary.items)}종</span></h2>
        <c:forEach var="it" items="${summary.items}">
          <div class="list-row">
            <div class="list-row__body">
              <p class="t-body3" style="color:var(--text-strong);font-weight:600"><c:out value="${it.bookName}" /></p>
              <p class="t-help num"><fmt:formatNumber value="${it.unitPrice}" pattern="#,##0" />원 × ${it.quantity}권</p>
            </div>
            <p class="price"><fmt:formatNumber value="${it.lineTotal}" pattern="#,##0" />원</p>
          </div>
        </c:forEach>
      </section>
    </div>

    <aside class="aside">
      <div class="card">
        <h2 class="t-title2 card-title">결제 금액</h2>
        <dl class="summary">
          <div><dt>상품 금액</dt><dd><fmt:formatNumber value="${summary.itemTotal}" pattern="#,##0" />원</dd></div>
          <div class="minus"><dt>쿠폰 할인</dt><dd>${summary.discount > 0 ? '−' : ''}<fmt:formatNumber value="${summary.discount}" pattern="#,##0" />원</dd></div>
          <div><dt>배송비</dt><dd><c:choose><c:when test="${summary.shippingFee == 0}">무료</c:when><c:otherwise><fmt:formatNumber value="${summary.shippingFee}" pattern="#,##0" />원</c:otherwise></c:choose></dd></div>
          <div class="total"><dt>결제할 금액</dt><dd><fmt:formatNumber value="${summary.payAmount}" pattern="#,##0" />원</dd></div>
        </dl>
        <p class="t-help" style="margin-top:12px">배송비는 할인 뒤 금액이 <fmt:formatNumber value="${freeShippingMin}" pattern="#,##0" />원 이상이면 무료예요.</p>
      </div>
      <div class="notice-box" style="margin-top:12px">연습용 서점이라 실제 결제는 일어나지 않고, 주문 기록만 DB에 저장돼요.</div>
      <div class="plate">
        <div class="plate__inner">
          <button class="btn btn-main" type="submit" name="action" value="place"><span class="num"><fmt:formatNumber value="${summary.payAmount}" pattern="#,##0" />원</span> 결제하기</button>
        </div>
      </div>
    </aside>
  </div>
</form>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
