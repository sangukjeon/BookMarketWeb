<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="회원 관리" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="members" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<form class="card member-filter" method="get" action="${ctx}/admin/members" role="search" aria-label="회원 찾기">
  <div class="member-filter__q">
    <label class="sr-only" for="mq">이름·아이디·이메일</label>
    <input class="input" id="mq" name="q" value="<c:out value='${q}'/>" placeholder="이름, 아이디, 이메일로 찾기">
  </div>
  <label class="sr-only" for="mprovider">가입 방식</label>
  <select class="select" id="mprovider" name="provider">
    <option value="">가입 방식 전체</option>
    <option value="LOCAL" ${provider == 'LOCAL' ? 'selected' : ''}>윤상문고 (자체)</option>
    <option value="KAKAO" ${provider == 'KAKAO' ? 'selected' : ''}>카카오</option>
    <option value="NAVER" ${provider == 'NAVER' ? 'selected' : ''}>네이버</option>
    <option value="GOOGLE" ${provider == 'GOOGLE' ? 'selected' : ''}>구글</option>
  </select>
  <label class="sr-only" for="mrole">권한</label>
  <select class="select" id="mrole" name="role">
    <option value="">권한 전체</option>
    <option value="USER" ${role == 'USER' ? 'selected' : ''}>일반 회원</option>
    <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>관리자</option>
  </select>
  <label class="sr-only" for="mstatus">상태</label>
  <select class="select" id="mstatus" name="status">
    <option value="">상태 전체</option>
    <option value="ACTIVE" ${mstatus == 'ACTIVE' ? 'selected' : ''}>정상</option>
    <option value="SUSPENDED" ${mstatus == 'SUSPENDED' ? 'selected' : ''}>정지됨</option>
    <option value="WITHDRAWN" ${mstatus == 'WITHDRAWN' ? 'selected' : ''}>탈퇴함</option>
  </select>
  <button class="btn btn-tint" type="submit"><ui:icon name="search" cls="icon-sm" />찾기</button>
</form>
<div class="row-between" style="margin:16px 0 12px;flex-wrap:wrap">
  <p class="t-help">전체 회원 <strong class="num" style="color:var(--text-strong)">${memberTotal}</strong>명 · 정지 <strong class="num" style="color:var(--text-strong)">${memberSuspended}</strong>명</p>
  <p class="t-help">찾은 회원 <strong class="num" style="color:var(--text-strong)">${fn:length(members)}</strong>명<c:if test="${not empty q or not empty provider or not empty role or not empty mstatus}"> · <a href="${ctx}/admin/members">조건 지우기</a></c:if></p>
</div>
<div class="tbl-wrap">
  <table class="tbl tbl--wide">
    <caption class="sr-only">회원 목록</caption>
    <thead><tr><th scope="col">번호</th><th scope="col">이름</th><th scope="col">아이디</th><th scope="col">가입 방식</th><th scope="col">권한</th><th scope="col">상태</th><th scope="col" class="r">주문</th><th scope="col" class="r">구매액</th><th scope="col">가입일</th><th scope="col" class="r">관리</th></tr></thead>
    <tbody>
      <c:forEach var="mb" items="${members}">
        <tr>
          <td class="num nowrap">${mb.memberNo}</td>
          <th scope="row" class="nowrap"><a href="${ctx}/admin/members/detail?no=${mb.memberNo}"><c:out value="${mb.name}" /></a></th>
          <td class="nowrap"><c:out value="${mb.social ? mb.email : mb.loginId}" /></td>
          <td class="nowrap">${mb.providerLabel}</td>
          <td class="nowrap"><c:choose><c:when test="${mb.admin}"><span class="pill pill--warn">관리자</span></c:when><c:otherwise>일반</c:otherwise></c:choose></td>
          <td class="nowrap"><c:choose><c:when test="${mb.withdrawn}"><span class="pill pill--neutral">탈퇴함</span></c:when><c:when test="${mb.suspended}"><span class="pill pill--danger">정지됨</span></c:when><c:otherwise><span class="pill pill--success">정상</span></c:otherwise></c:choose></td>
          <td class="r nowrap">${mb.orderCount}건</td>
          <td class="r nowrap"><fmt:formatNumber value="${mb.totalSpent}" pattern="#,##0" />원</td>
          <td class="num nowrap">${mb.createdAtText}</td>
          <td class="r nowrap"><a class="btn btn-sub btn-s" href="${ctx}/admin/members/detail?no=${mb.memberNo}">보기</a></td>
        </tr>
      </c:forEach>
      <c:if test="${empty members}"><tr class="empty-row"><td colspan="10">조건에 맞는 회원이 없어요</td></tr></c:if>
    </tbody>
  </table>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
