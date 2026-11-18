<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="로그인" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<c:set var="idOpen" value="${not empty error or not empty loginId}" />
<div class="auth-wrap">
<%@ include file="/WEB-INF/views/common/auth-intro.jspf" %>
<div class="auth">
  <div class="auth__head">
    <span class="logo__name auth__logo">전상욱 마켓</span>
    <h1 class="t-heading">로그인하고 쇼핑을 이어가세요</h1>
    <p class="t-body3 muted">주문하고 지난 영수증을 볼 수 있어요.</p>
  </div>

  <div class="social">
    <a class="btn-social btn-social--kakao" href="${ctx}/oauth/kakao/login?next=${fn:escapeXml(next)}"><ui:brand name="kakao" /><span>카카오로 계속하기</span></a>
    <a class="btn-social btn-social--naver" href="${ctx}/oauth/naver/login?next=${fn:escapeXml(next)}"><ui:brand name="naver" /><span>네이버로 계속하기</span></a>
    <a class="btn-social btn-social--google" href="${ctx}/oauth/google/login?next=${fn:escapeXml(next)}"><ui:brand name="google" /><span>Google로 계속하기</span></a>
  </div>
  <p class="t-help auth__note"><ui:icon name="info" cls="icon-sm" />지금은 테스트용 목업 로그인이에요. 실제 계정 정보는 쓰지 않아요.</p>

  <div class="divider">다른 방법으로 계속하기</div>
  <div class="round-actions">
    <button type="button" class="round-btn" data-toggle="id-login" aria-expanded="${idOpen}" aria-controls="id-login">
      <span class="round-btn__icon"><ui:icon name="user-round" /></span><span>아이디</span>
    </button>
    <a class="round-btn" href="${ctx}/signup?next=${fn:escapeXml(next)}">
      <span class="round-btn__icon"><ui:icon name="user-plus" /></span><span>회원가입</span>
    </a>
  </div>

  <form class="card id-login" id="id-login" method="post" action="${ctx}/login" novalidate ${idOpen ? '' : 'hidden'}>
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <input type="hidden" name="next" value="<c:out value='${next}'/>">
    <h2 class="t-title2 card-title">아이디로 로그인</h2>
    <c:if test="${not empty error}">
      <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
    </c:if>
    <div class="field">
      <label for="loginId">아이디</label>
      <input class="input" id="loginId" name="loginId" value="<c:out value='${loginId}'/>" autocomplete="username" required ${not empty error ? 'aria-invalid="true"' : ''}>
    </div>
    <div class="field">
      <label for="password">비밀번호</label>
      <input class="input" id="password" name="password" type="password" autocomplete="current-password" required ${not empty error ? 'aria-invalid="true"' : ''}>
    </div>
    <button class="btn btn-main btn-block" type="submit" style="margin-top:24px">로그인하기</button>
  </form>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
