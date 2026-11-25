<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="곰 독서 성향 테스트" />
<c:set var="containerCls" value="container--read" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<section class="taste-intro">
  <img src="${ctx}/images/mascot/bear-search.png" alt="" width="120" height="115">
  <div>
    <h1 class="t-heading">나는 어떤 책 곰일까?</h1>
    <p class="t-body3 muted">질문 5개에 답하면 나와 닮은 곰과 어울리는 장르, 추천 책 3권을 알려 드려요.
      <c:choose>
        <c:when test="${empty me}">로그인하고 하면 처음 한 번 500원 쿠폰도 드려요.</c:when>
        <c:otherwise>처음 끝내면 500원 쿠폰을 쿠폰함에 넣어 드려요.</c:otherwise>
      </c:choose>
    </p>
    <c:if test="${not empty myBear}"><p class="t-help" style="margin-top:8px">지난번 결과: <a href="${ctx}/taste/result?type=${myBear.slug}">${myBear.label}</a></p></c:if>
  </div>
</section>

<form class="taste-form" method="post" action="${ctx}/taste" novalidate>
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <c:if test="${not empty error}">
    <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
  </c:if>
  <c:forEach var="qn" items="${questions}" varStatus="st">
    <fieldset class="card taste-q">
      <legend class="t-title2"><span class="taste-q__no num">${st.count}</span>${qn.text}</legend>
      <div class="taste-q__options">
        <c:forEach var="op" items="${qn.options}">
          <label class="taste-opt"><input type="radio" name="q${st.count}" value="${op.value}" ${answers[st.index] == op.value ? 'checked' : ''} required><span>${op.text}</span></label>
        </c:forEach>
      </div>
    </fieldset>
  </c:forEach>
  <button class="btn btn-main btn-block" type="submit" style="margin-top:16px"><ui:icon name="paw-print" />결과 보기</button>
</form>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
