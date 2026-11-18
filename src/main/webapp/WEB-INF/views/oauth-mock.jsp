<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="${provider.label()} 로그인 (목업)" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="auth">
  <div class="banner banner--warn" style="margin:8px 0 16px">
    <ui:icon name="triangle-alert" />
    <span>테스트용 목업 화면이에요. 실제 ${provider.label()} 로그인 화면이 아니며, 여기 입력한 정보는 전상욱 마켓 DB에만 저장돼요.</span>
  </div>
  <div class="page-head">
    <h1 class="t-heading">${provider.label()} 계정으로 계속하기</h1>
    <p class="t-body3 muted">실제 연동 뒤에는 이 단계에서 ${provider.label()}가 이름과 이메일을 전상욱 마켓에 넘겨줘요.</p>
  </div>
  <form class="card" method="post" action="${ctx}/oauth/mock" novalidate>
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <input type="hidden" name="provider" value="${fn:toLowerCase(provider.id())}">
    <input type="hidden" name="state" value="<c:out value='${state}'/>">
    <c:if test="${not empty error}">
      <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
    </c:if>
    <div class="field">
      <label for="name">이름</label>
      <input class="input" id="name" name="name" value="<c:out value='${empty name ? "테스트 사용자" : name}'/>" required>
    </div>
    <div class="field">
      <label for="email">이메일</label>
      <input class="input" id="email" name="email" type="email" value="<c:out value='${empty email ? "tester@example.com" : email}'/>" required aria-describedby="emailHelp">
      <span class="help" id="emailHelp">같은 이메일로 다시 들어오면 같은 회원으로 로그인돼요.</span>
    </div>
    <div class="attach" style="margin-top:16px">
      <p class="attach__title">전상욱 마켓이 받는 정보</p>
      <p class="t-help">이름, 이메일 (프로필 식별용)</p>
    </div>
    <div class="row" style="margin-top:24px">
      <button class="btn btn-sub" type="submit" name="action" value="cancel" formnovalidate>취소하기</button>
      <button class="btn btn-main" type="submit" name="action" value="allow" style="flex:1">동의하고 계속하기</button>
    </div>
  </form>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
