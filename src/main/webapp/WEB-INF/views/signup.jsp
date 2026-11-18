<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="회원가입" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="auth-wrap">
<%@ include file="/WEB-INF/views/common/auth-intro.jspf" %>
<div class="auth">
  <div class="page-head">
    <h1 class="t-heading">회원가입</h1>
    <p class="t-body3 muted">비밀번호는 암호화해서 저장해요. 원래 비밀번호는 아무도 볼 수 없어요.</p>
  </div>
  <form class="card" method="post" action="${ctx}/signup" novalidate>
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <input type="hidden" name="next" value="<c:out value='${next}'/>">
    <c:if test="${not empty error}">
      <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
    </c:if>
    <div class="field">
      <label for="loginId">아이디</label>
      <input class="input" id="loginId" name="loginId" value="<c:out value='${loginId}'/>" autocomplete="username" required aria-describedby="idHelp">
      <span class="help" id="idHelp">영문과 숫자 4~20자</span>
    </div>
    <div class="field">
      <label for="password">비밀번호</label>
      <input class="input" id="password" name="password" type="password" autocomplete="new-password" required aria-describedby="pwHelp">
      <span class="help" id="pwHelp">영문과 숫자를 섞어 8자 이상</span>
    </div>
    <div class="field">
      <label for="password2">비밀번호 확인</label>
      <input class="input" id="password2" name="password2" type="password" autocomplete="new-password" required>
    </div>
    <div class="field">
      <label for="name">이름</label>
      <input class="input" id="name" name="name" value="<c:out value='${name}'/>" autocomplete="name" required>
    </div>
    <div class="field">
      <label for="phone">연락처</label>
      <input class="input" id="phone" name="phone" value="<c:out value='${phone}'/>" inputmode="tel" autocomplete="tel" placeholder="010-1234-5678" required>
    </div>
    <div class="field">
      <label for="address">기본 배송지 <span class="muted">(선택)</span></label>
      <input class="input" id="address" name="address" value="<c:out value='${address}'/>" autocomplete="street-address">
    </div>
    <button class="btn btn-main btn-block" type="submit" style="margin-top:24px">가입하기</button>
  </form>
  <p class="t-body3 muted" style="text-align:center;margin-top:16px">
    이미 회원이세요? <a class="text-btn" href="${ctx}/login?next=${fn:escapeXml(next)}">로그인하기</a>
  </p>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
