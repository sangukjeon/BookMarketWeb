<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="알림" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head">
  <h1 class="t-heading">알림</h1>
  <p class="t-body3 muted">재입고·배송·쿠폰 소식을 모아 보여요. 최근 30개까지 보여요.</p>
</div>
<c:choose>
  <c:when test="${empty notifications}">
    <div class="card empty">
      <img class="empty__mascot" src="${ctx}/images/mascot/bear-search.png" alt="" width="132" height="126">
      <h2 class="t-title2">아직 알림이 없어요</h2>
      <p class="t-body3 muted">품절된 책에서 "재입고 알림 받기"를 누르면 다시 들어올 때 여기로 알려 드려요.</p>
    </div>
  </c:when>
  <c:otherwise>
    <ul class="noti-list card">
      <c:forEach var="n" items="${notifications}">
        <li class="noti${n.read ? '' : ' is-new'}">
          <span class="noti__dot" aria-hidden="true"></span>
          <div class="noti__body">
            <c:choose>
              <c:when test="${not empty n.link}"><a href="${ctx}${n.link}"><c:out value="${n.message}" /></a></c:when>
              <c:otherwise><c:out value="${n.message}" /></c:otherwise>
            </c:choose>
            <span class="t-help num">${n.createdAtText}<c:if test="${not n.read}"> · 새 알림</c:if></span>
          </div>
        </li>
      </c:forEach>
    </ul>
  </c:otherwise>
</c:choose>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
