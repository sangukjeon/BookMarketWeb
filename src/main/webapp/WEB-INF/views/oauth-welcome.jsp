<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="배송 정보 입력" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="auth-wrap">
<%@ include file="/WEB-INF/views/common/auth-intro.jspf" %>
<div class="auth">
  <div class="page-head">
    <h1 class="t-heading">거의 다 됐어요</h1>
    <p class="t-body3 muted">${me.providerLabel} 계정으로 가입했어요. 주문할 때 쓸 정보를 넣어 두면 주문서가 자동으로 채워져요.</p>
  </div>
  <form class="card" method="post" action="${ctx}/oauth/welcome" novalidate>
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <c:if test="${not empty error}">
      <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
    </c:if>
    <c:if test="${not empty me.email}">
      <div class="field">
        <label for="email">이메일</label>
        <input class="input" id="email" value="<c:out value='${me.email}'/>" disabled>
        <span class="help">${me.providerLabel}에서 받아 온 이메일이에요</span>
      </div>
    </c:if>
    <div class="field">
      <label for="name">이름</label>
      <input class="input" id="name" name="name" value="<c:out value='${me.name}'/>" autocomplete="name" required>
    </div>
    <div class="field">
      <label for="phone">연락처 <span class="muted">(선택)</span></label>
      <input class="input" id="phone" name="phone" value="<c:out value='${me.phone}'/>" inputmode="tel" autocomplete="tel" placeholder="010-1234-5678">
    </div>
    <div class="field">
      <label for="address">기본 배송지 <span class="muted">(선택)</span></label>
      <input class="input" id="address" name="address" value="<c:out value='${me.address}'/>" autocomplete="street-address">
    </div>
    <button class="btn btn-main btn-block" type="submit" style="margin-top:24px">저장하고 시작하기</button>
  </form>
  <p class="t-body3 muted" style="text-align:center;margin-top:16px">
    <a class="text-btn" href="${ctx}${empty next ? '/books' : next}">나중에 입력하기</a>
  </p>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
