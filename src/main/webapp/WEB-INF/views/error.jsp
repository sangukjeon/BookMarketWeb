<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}" />
<c:set var="title" value="문제가 생겼어요" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="card empty" style="max-width:480px;margin:24px auto">
  <img class="empty__mascot" src="${ctx}/images/mascot/bear-oops.png" alt="" width="132" height="124">
  <h1 class="t-title2">
    <c:choose>
      <c:when test="${code == 404}">찾는 화면이 없어요</c:when>
      <c:when test="${code == 403}">들어갈 수 없는 화면이에요</c:when>
      <c:otherwise>잠시 문제가 생겼어요</c:otherwise>
    </c:choose>
  </h1>
  <p class="t-body3 muted">
    <c:choose>
      <c:when test="${not empty requestScope['jakarta.servlet.error.message'] and code != 500 and code != 404}"><c:out value="${requestScope['jakarta.servlet.error.message']}" /></c:when>
      <c:otherwise>주소를 확인하거나 처음 화면에서 다시 시작해 주세요.</c:otherwise>
    </c:choose>
  </p>
  <a class="btn btn-sub" href="${ctx}/books">처음 화면으로</a>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
