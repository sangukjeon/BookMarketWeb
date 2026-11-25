<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="쿠폰함" />
<c:set var="nav" value="coupons" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head">
  <h1 class="t-heading">쿠폰함</h1>
  <p class="t-body3 muted">받은 쿠폰 번호를 등록하면 주문서에서 골라 쓸 수 있어요. 쿠폰은 한 번씩 쓸 수 있고, 그 주문을 취소하면 다시 쓸 수 있어요.</p>
</div>

<form class="card" method="post" action="${ctx}/coupons" novalidate>
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <input type="hidden" name="next" value="<c:out value='${next}'/>">
  <label class="t-title2 card-title" for="code" style="display:block">쿠폰 번호 등록</label>
  <div class="input-row">
    <input class="input" id="code" name="code" value="<c:out value='${code}'/>" placeholder="예: BOOK3000" autocapitalize="characters" autocomplete="off" ${not empty error ? 'aria-invalid="true"' : ''} aria-describedby="codeHelp">
    <button class="btn btn-tint" type="submit">등록하기</button>
  </div>
  <c:choose>
    <c:when test="${not empty error}"><p class="help help--error" id="codeHelp" role="alert"><c:out value="${error}" /></p></c:when>
    <c:otherwise><p class="help" id="codeHelp">영문 대소문자는 구분하지 않아요.</p></c:otherwise>
  </c:choose>
</form>

<h2 class="t-title2" style="margin:24px 0 12px">내 쿠폰 <span class="muted num">${fn:length(wallet)}</span></h2>
<c:choose>
  <c:when test="${empty wallet}">
    <div class="card empty">
      <img class="empty__mascot" src="${ctx}/images/mascot/bear-search.png" alt="" width="132" height="126">
      <h2 class="t-title2">아직 쿠폰이 없어요</h2>
      <p class="t-body3 muted">받은 쿠폰 번호를 위에 넣어 등록해 주세요.</p>
    </div>
  </c:when>
  <c:otherwise>
    <ul class="coupon-list">
      <c:forEach var="cp" items="${wallet}">
        <li class="coupon-ticket${cp.usable ? '' : ' is-off'}">
          <div class="coupon-ticket__benefit">${cp.benefitLabel}</div>
          <div class="coupon-ticket__body">
            <p class="t-title2"><c:out value="${cp.name}" /></p>
            <p class="t-help"><span class="num"><c:out value="${cp.code}" /></span> · <span class="num">${cp.registeredAtText}</span> 등록<c:if test="${cp.minAmount > 0}"> · <fmt:formatNumber value="${cp.minAmount}" pattern="#,##0" />원 이상</c:if></p>
          </div>
          <span class="pill ${cp.usable ? 'pill--success' : (cp.active ? 'pill--neutral' : 'pill--danger')}">${cp.walletStatus}</span>
        </li>
      </c:forEach>
    </ul>
  </c:otherwise>
</c:choose>
<c:if test="${not empty next}"><p style="margin-top:16px;text-align:center"><a class="btn btn-sub" href="${ctx}${next}">주문서로 돌아가기</a></p></c:if>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
