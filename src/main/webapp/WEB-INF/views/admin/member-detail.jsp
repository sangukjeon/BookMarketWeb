<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="회원 정보" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="members" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<p style="margin-bottom:12px"><a class="t-help" href="${ctx}/admin/members"><ui:icon name="arrow-left" cls="icon-sm" />회원 목록으로</a></p>

<c:if test="${not empty tempPassword}">
  <div class="banner banner--info" role="status" style="margin-bottom:16px">
    <ui:icon name="key-round" />
    <span>임시 비밀번호: <strong class="num temp-pw"><c:out value="${tempPassword}" /></strong> — 이 화면을 벗어나면 다시 볼 수 없어요. 회원에게 전달하고, 로그인한 뒤 바꾸도록 안내해 주세요.</span>
  </div>
</c:if>

<div class="member-detail">
  <section class="card" aria-labelledby="who">
    <div class="row-between" style="align-items:flex-start">
      <div>
        <h2 class="t-title1" id="who"><c:out value="${target.name}" /></h2>
        <p class="t-body3 muted" style="margin-top:4px">${target.providerLabel} 계정 · 회원 번호 ${target.memberNo}</p>
      </div>
      <div class="row" style="gap:6px">
        <c:if test="${target.admin}"><span class="pill pill--warn">관리자</span></c:if>
        <c:choose><c:when test="${target.suspended}"><span class="pill pill--danger">정지됨</span></c:when><c:otherwise><span class="pill pill--success">정상</span></c:otherwise></c:choose>
      </div>
    </div>
    <dl class="kv" style="margin-top:20px">
      <dt>아이디</dt><dd><c:out value="${target.loginId}" /></dd>
      <dt>이메일</dt><dd><c:out value="${empty target.email ? '-' : target.email}" /></dd>
      <dt>연락처</dt><dd><c:out value="${empty target.phone ? '-' : target.phone}" /></dd>
      <dt>주소</dt><dd><c:out value="${empty target.address ? '-' : target.address}" /></dd>
      <dt>가입일</dt><dd class="num">${target.createdAtText}</dd>
      <dt>구매</dt><dd><strong class="num">${target.orderCount}</strong>건 · <strong class="num"><fmt:formatNumber value="${target.totalSpent}" pattern="#,##0" /></strong>원 <span class="muted">(취소 제외)</span></dd>
    </dl>
  </section>

  <section class="card" aria-labelledby="manage">
    <h2 class="t-title2 card-title" id="manage">계정 관리</h2>
    <c:choose>
      <c:when test="${self}">
        <p class="t-body3 muted">내 계정이에요. 상태와 권한은 다른 관리자가 바꿀 수 있어요.</p>
      </c:when>
      <c:otherwise>
        <div class="manage-list">
          <form method="post" action="${ctx}/admin/members/status"
                data-confirm="${target.suspended ? '계정 정지를 풀까요?' : '이 계정을 정지할까요?'}"
                data-confirm-desc="${target.suspended ? '다시 로그인하고 주문할 수 있어요.' : '로그인할 수 없고, 이미 로그인해 있으면 바로 로그아웃돼요. 주문 기록은 그대로 남아요.'}"
                data-confirm-ok="${target.suspended ? '정지 풀기' : '정지하기'}">
            <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
            <input type="hidden" name="no" value="${target.memberNo}">
            <input type="hidden" name="value" value="${target.suspended ? 'ACTIVE' : 'SUSPENDED'}">
            <div class="manage-row">
              <div><p class="t-label">${target.suspended ? '계정 정지 풀기' : '계정 정지'}</p><p class="t-help">${target.suspended ? '정지된 계정을 다시 쓸 수 있게 해요' : '로그인과 주문을 막아요'}</p></div>
              <button class="btn btn-sub btn-s" type="submit" ${target.suspended ? '' : 'style="color:var(--danger-strong)"'}><ui:icon name="${target.suspended ? 'user-round-check' : 'user-round-x'}" cls="icon-sm" />${target.suspended ? '정지 풀기' : '정지하기'}</button>
            </div>
          </form>
          <form method="post" action="${ctx}/admin/members/role"
                data-confirm="${target.admin ? '일반 회원으로 바꿀까요?' : '관리자로 바꿀까요?'}"
                data-confirm-desc="${target.admin ? '관리자 화면에 들어올 수 없게 돼요.' : '매출, 주문, 회원 정보를 모두 볼 수 있게 돼요.'}"
                data-confirm-ok="권한 바꾸기">
            <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
            <input type="hidden" name="no" value="${target.memberNo}">
            <input type="hidden" name="value" value="${target.admin ? 'USER' : 'ADMIN'}">
            <div class="manage-row">
              <div><p class="t-label">권한</p><p class="t-help">지금은 ${target.admin ? '관리자' : '일반 회원'}이에요</p></div>
              <button class="btn btn-sub btn-s" type="submit"><ui:icon name="shield" cls="icon-sm" />${target.admin ? '일반 회원으로 바꾸기' : '관리자로 바꾸기'}</button>
            </div>
          </form>
        </div>
      </c:otherwise>
    </c:choose>
    <c:if test="${not target.social}">
      <form method="post" action="${ctx}/admin/members/password" data-confirm="비밀번호를 초기화할까요?" data-confirm-desc="지금 비밀번호는 쓸 수 없게 되고, 임시 비밀번호를 한 번만 보여 드려요." data-confirm-ok="초기화하기">
        <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
        <input type="hidden" name="no" value="${target.memberNo}">
        <div class="manage-row">
          <div><p class="t-label">비밀번호 초기화</p><p class="t-help">임시 비밀번호를 만들어요</p></div>
          <button class="btn btn-sub btn-s" type="submit"><ui:icon name="key-round" cls="icon-sm" />초기화하기</button>
        </div>
      </form>
    </c:if>
    <c:if test="${target.social}"><p class="t-help" style="margin-top:12px">${target.providerLabel} 로그인 회원이라 비밀번호가 없어요.</p></c:if>
  </section>
</div>

<h2 class="t-title2" style="margin:24px 0 12px">주문 내역 <span class="muted num">${fn:length(orders)}</span></h2>
<div class="tbl-wrap">
  <table class="tbl tbl--wide">
    <caption class="sr-only">이 회원의 주문 목록</caption>
    <thead><tr><th scope="col">주문번호</th><th scope="col">상품</th><th scope="col" class="r">결제 금액</th><th scope="col">상태</th><th scope="col">주문 일시</th></tr></thead>
    <tbody>
      <c:forEach var="o" items="${orders}">
        <tr>
          <td class="num nowrap"><a href="${ctx}/orders/receipt?id=${o.orderId}">${o.orderNo}</a></td>
          <th scope="row" style="min-width:180px"><c:out value="${o.firstItemName}" /><c:if test="${o.itemCount > 1}"> <span class="muted">외 ${o.itemCount - 1}종</span></c:if></th>
          <td class="r nowrap"><fmt:formatNumber value="${o.payAmount}" pattern="#,##0" />원</td>
          <td class="nowrap"><c:choose><c:when test="${o.cancelled}"><span class="pill pill--danger">취소됨</span></c:when><c:otherwise><span class="pill pill--success">주문완료</span></c:otherwise></c:choose></td>
          <td class="num nowrap">${o.orderedAtText}</td>
        </tr>
      </c:forEach>
      <c:if test="${empty orders}"><tr class="empty-row"><td colspan="5">아직 주문이 없어요</td></tr></c:if>
    </tbody>
  </table>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
