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
  <a class="list-row" href="${ctx}/coupons"><ui:icon name="ticket-percent" /><span class="list-row__body t-title2">쿠폰함</span><ui:icon name="chevron-right" cls="chev" /></a>
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

<section class="card" id="addresses" aria-labelledby="addr-title" style="margin-top:12px">
  <div class="row-between card-title">
    <h2 class="t-title2" id="addr-title">배송지 <span class="muted num">${fn:length(addresses)}/${maxAddresses}</span></h2>
  </div>
  <c:if test="${not empty addrError}">
    <div class="banner banner--danger" role="alert" style="margin-bottom:12px"><ui:icon name="triangle-alert" /><span><c:out value="${addrError}" /></span></div>
  </c:if>
  <c:choose>
    <c:when test="${empty addresses}"><p class="t-body3 muted" style="margin-bottom:12px">저장한 배송지가 없어요. 집·회사 주소를 저장해 두면 주문서에서 한 번에 골라요.</p></c:when>
    <c:otherwise>
      <ul class="addr-list">
        <c:forEach var="ad" items="${addresses}">
          <li class="addr">
            <div class="addr__body">
              <p class="t-title2"><c:out value="${ad.label}" /><c:if test="${ad.defaultAddress}"> <span class="pill pill--s pill--info">기본</span></c:if></p>
              <p class="t-body3"><c:out value="${ad.receiverName}" /> · <span class="num"><c:out value="${ad.phone}" /></span></p>
              <p class="t-body3 muted"><c:out value="${ad.address}" /></p>
            </div>
            <div class="addr__actions">
              <c:if test="${not ad.defaultAddress}">
                <form method="post" action="${ctx}/mypage/address/default">
                  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                  <input type="hidden" name="id" value="${ad.addressId}">
                  <button class="btn btn-sub btn-s" type="submit">기본으로</button>
                </form>
              </c:if>
              <form method="post" action="${ctx}/mypage/address/delete" data-confirm="이 배송지를 지울까요?" data-confirm-ok="지우기">
                <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                <input type="hidden" name="id" value="${ad.addressId}">
                <button class="icon-btn" type="submit" aria-label="<c:out value='${ad.label}'/> 배송지 지우기"><ui:icon name="trash-2" /></button>
              </form>
            </div>
          </li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
  <c:if test="${fn:length(addresses) < maxAddresses}">
    <details class="fold" ${not empty addrError ? 'open' : ''}>
      <summary><ui:icon name="plus" cls="icon-sm" />배송지 추가하기</summary>
      <form method="post" action="${ctx}/mypage/address" novalidate style="margin-top:12px">
        <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
        <div class="field-grid field-grid--2">
          <div class="field"><label for="ad-label">배송지 이름</label><input class="input" id="ad-label" name="label" maxlength="20" placeholder="집, 회사" value="<c:out value='${addrForm.label}'/>"></div>
          <div class="field"><label for="ad-name">받는 분</label><input class="input" id="ad-name" name="receiverName" value="<c:out value='${empty addrForm ? me.name : addrForm.receiverName}'/>"></div>
        </div>
        <div class="field"><label for="ad-phone">연락처</label><input class="input" id="ad-phone" name="phone" inputmode="tel" placeholder="010-1234-5678" value="<c:out value='${empty addrForm ? me.phone : addrForm.phone}'/>"></div>
        <div class="field"><label for="ad-addr">주소</label><input class="input" id="ad-addr" name="address" placeholder="예: 서울시 종로구 세종대로 1, 101동 1001호" value="<c:out value='${addrForm.address}'/>"></div>
        <label class="check"><input type="checkbox" name="makeDefault">기본 배송지로 쓰기</label>
        <div class="row" style="margin-top:16px;justify-content:flex-end"><button class="btn btn-tint" type="submit">배송지 저장하기</button></div>
      </form>
    </details>
  </c:if>
</section>

<c:if test="${not me.social}">
  <section class="card" aria-labelledby="pw-title" style="margin-top:12px">
    <details class="fold" ${not empty pwError ? 'open' : ''}>
      <summary class="t-title2" id="pw-title"><ui:icon name="lock" cls="icon-sm" />비밀번호 바꾸기</summary>
      <form method="post" action="${ctx}/mypage/password" novalidate style="margin-top:12px">
        <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
        <c:if test="${not empty pwError}"><div class="banner banner--danger" role="alert" style="margin-bottom:12px"><ui:icon name="triangle-alert" /><span><c:out value="${pwError}" /></span></div></c:if>
        <div class="field"><label for="pw-cur">지금 비밀번호</label><input class="input" id="pw-cur" name="currentPassword" type="password" autocomplete="current-password"></div>
        <div class="field"><label for="pw-new">새 비밀번호</label><input class="input" id="pw-new" name="newPassword" type="password" autocomplete="new-password" aria-describedby="pw-help"><span class="help" id="pw-help">영문과 숫자를 섞어 8자 이상</span></div>
        <div class="field"><label for="pw-new2">새 비밀번호 확인</label><input class="input" id="pw-new2" name="newPassword2" type="password" autocomplete="new-password"></div>
        <div class="row" style="margin-top:16px;justify-content:flex-end"><button class="btn btn-tint" type="submit">비밀번호 바꾸기</button></div>
      </form>
    </details>
  </section>
</c:if>

<form method="post" action="${ctx}/logout" style="margin-top:16px">
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <button class="btn btn-sub btn-block" type="submit"><ui:icon name="log-out" />로그아웃하기</button>
</form>

<c:if test="${not me.admin}">
  <details class="fold fold--danger" ${not empty leaveError ? 'open' : ''} style="margin-top:24px">
    <summary>회원 탈퇴</summary>
    <form method="post" action="${ctx}/mypage/withdraw" novalidate style="margin-top:12px" data-confirm="정말 탈퇴할까요?" data-confirm-desc="이름·연락처·주소·찜·장바구니·쿠폰함이 지워지고 되돌릴 수 없어요. 주문 기록은 영수증 보관을 위해 남아요." data-confirm-ok="탈퇴하기">
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <c:if test="${not empty leaveError}"><div class="banner banner--danger" role="alert" style="margin-bottom:12px"><ui:icon name="triangle-alert" /><span><c:out value="${leaveError}" /></span></div></c:if>
      <p class="t-body3 muted" style="margin-bottom:12px">탈퇴하면 개인정보는 지우고, 주문·리뷰 기록은 "탈퇴한 회원"으로 남아요. 배송이 끝나지 않은 주문이 있으면 탈퇴할 수 없어요.</p>
      <c:choose>
        <c:when test="${me.social}"><div class="field"><label for="leave-confirm">확인을 위해 <strong>탈퇴</strong>라고 입력해 주세요</label><input class="input" id="leave-confirm" name="confirm" autocomplete="off"></div></c:when>
        <c:otherwise><div class="field"><label for="leave-pw">비밀번호</label><input class="input" id="leave-pw" name="password" type="password" autocomplete="current-password"></div></c:otherwise>
      </c:choose>
      <button class="btn btn-sub btn-block" type="submit" style="color:var(--danger-strong);margin-top:8px"><ui:icon name="user-round-x" />탈퇴하기</button>
    </form>
  </details>
</c:if>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
