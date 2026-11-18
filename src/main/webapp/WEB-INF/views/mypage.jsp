<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="내 정보" />
<c:set var="nav" value="mypage" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head row-between">
  <div>
    <h1 class="t-heading"><c:out value="${me.name}" />님</h1>
    <p class="t-body3 muted">${me.providerLabel} 계정<c:if test="${me.social}"> · <c:out value="${me.email}" /></c:if><c:if test="${not me.social}"> · <c:out value="${me.loginId}" /></c:if></p>
  </div>
  <c:if test="${me.admin}"><a class="pill pill--warn" href="${ctx}/admin" style="text-decoration:none">관리자</a></c:if>
</div>

<div class="card card--flush" style="margin-bottom:12px">
  <a class="list-row" href="${ctx}/orders"><ui:icon name="receipt" /><span class="list-row__body t-title2">주문 내역 보기</span><ui:icon name="chevron-right" cls="chev" /></a>
  <a class="list-row" href="${ctx}/wishlist"><ui:icon name="heart" /><span class="list-row__body t-title2">찜한 책</span><ui:icon name="chevron-right" cls="chev" /></a>
  <a class="list-row" href="${ctx}/cart"><ui:icon name="shopping-cart" /><span class="list-row__body t-title2">장바구니</span><span class="muted num">${cartCount}권</span><ui:icon name="chevron-right" cls="chev" /></a>
</div>

<form class="card" method="post" action="${ctx}/mypage" novalidate>
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <h2 class="t-title2 card-title">기본 정보</h2>
  <c:if test="${not empty error}">
    <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
  </c:if>
  <div class="field">
    <label for="name">이름</label>
    <input class="input" id="name" name="name" value="<c:out value='${me.name}'/>" required>
  </div>
  <div class="field">
    <label for="phone">연락처</label>
    <input class="input" id="phone" name="phone" value="<c:out value='${me.phone}'/>" inputmode="tel" placeholder="010-1234-5678">
  </div>
  <div class="field">
    <label for="address">기본 배송지</label>
    <input class="input" id="address" name="address" value="<c:out value='${me.address}'/>" placeholder="주문할 때 자동으로 채워져요">
  </div>
  <div class="row" style="margin-top:24px;justify-content:flex-end">
    <button class="btn btn-tint" type="submit">저장하기</button>
  </div>
</form>

<form method="post" action="${ctx}/logout" style="margin-top:16px">
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <button class="btn btn-sub btn-block" type="submit"><ui:icon name="log-out" />로그아웃하기</button>
</form>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
