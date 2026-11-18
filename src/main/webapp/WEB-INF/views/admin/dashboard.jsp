<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="매출 요약" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="dashboard" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<p class="t-help" style="margin-bottom:12px">매출은 취소되지 않은 주문만 계산해요.</p>
<div class="stat-grid">
  <div class="card stat">
    <p class="stat__label"><ui:icon name="wallet" cls="icon-sm" />오늘 매출</p>
    <p class="stat__value"><fmt:formatNumber value="${summary.todayRevenue}" pattern="#,##0" /><small>원</small></p>
    <p class="t-help">오늘 주문 <strong class="num">${summary.todayOrders}</strong>건</p>
  </div>
  <div class="card stat">
    <p class="stat__label"><ui:icon name="trending-up" cls="icon-sm" />전체 매출</p>
    <p class="stat__value"><fmt:formatNumber value="${summary.revenue}" pattern="#,##0" /><small>원</small></p>
    <p class="t-help">쿠폰 할인 <fmt:formatNumber value="${summary.discount}" pattern="#,##0" />원 포함</p>
  </div>
  <div class="card stat">
    <p class="stat__label"><ui:icon name="receipt" cls="icon-sm" />주문 수</p>
    <p class="stat__value num">${summary.orders}<small>건</small></p>
    <p class="t-help">취소 <strong class="num">${summary.cancelled}</strong>건 제외</p>
  </div>
  <div class="card stat">
    <p class="stat__label"><ui:icon name="ban" cls="icon-sm" />취소율</p>
    <p class="stat__value num">${summary.cancelRate}<small>%</small></p>
    <div class="bar" role="img" aria-label="취소율 ${summary.cancelRate}%"><span style="width:${summary.cancelRate}%"></span></div>
  </div>
</div>

<a class="card member-strip" href="${ctx}/admin/members" aria-label="회원 관리로 가기">
  <span class="member-strip__icon"><ui:icon name="users" /></span>
  <span class="member-strip__body">
    <span class="t-label">회원</span>
    <span class="t-help">전체 <strong class="num">${memberTotal}</strong>명 · 오늘 가입 <strong class="num">${memberToday}</strong>명 · 정지 <strong class="num">${memberSuspended}</strong>명</span>
  </span>
  <span class="t-help member-strip__go">회원 관리<ui:icon name="chevron-right" cls="icon-sm" /></span>
</a>

<section class="card" style="margin-top:20px" aria-labelledby="week-title">
  <div class="row-between">
    <h2 class="t-title2" id="week-title">최근 7일 매출</h2>
    <p class="t-help">7일 합계 <strong class="num" style="color:var(--text-strong)"><fmt:formatNumber value="${weekTotal}" pattern="#,##0" />원</strong></p>
  </div>
  <div class="bar-chart" role="img" aria-label="최근 7일 날짜별 매출 막대그래프">
    <c:forEach var="d" items="${days}">
      <div class="bar-chart__col ${d.today ? 'is-today' : ''}${d.amount == 0 ? ' is-empty' : ''}">
        <span class="bar-chart__val"><c:if test="${d.amount > 0}"><fmt:formatNumber value="${d.amount / 10000}" pattern="#,##0.#" />만</c:if></span>
        <span class="bar-chart__track"><span class="bar-chart__bar" style="height:${d.amount * 100 / dayMax}%"></span></span>
        <span class="bar-chart__label">${d.today ? '오늘' : d.label}</span>
      </div>
    </c:forEach>
  </div>
</section>

<div class="split admin-grid" style="margin-top:20px">
  <section class="card card--flush" aria-labelledby="top-title">
    <h2 class="t-title2" id="top-title" style="padding:16px 16px 8px">많이 팔린 책 TOP 5</h2>
    <c:choose>
      <c:when test="${empty summary.topBooks}"><p class="t-body3 muted" style="padding:8px 16px 20px">아직 팔린 책이 없어요</p></c:when>
      <c:otherwise>
        <c:set var="maxQty" value="${summary.topBooks[0].quantity}" />
        <ol class="top-list">
          <c:forEach var="t" items="${summary.topBooks}" varStatus="st">
            <li>
              <span class="rank num">${st.count}</span>
              <div class="top-list__body">
                <div class="row-between"><a href="${ctx}/books/detail?id=${fn:escapeXml(t.bookId)}" class="t-label top-list__name"><c:out value="${t.name}" /></a>
                  <span class="t-help num nowrap"><strong style="color:var(--text-strong)">${t.quantity}</strong>권 · <fmt:formatNumber value="${t.amount}" pattern="#,##0" />원</span></div>
                <div class="bar" aria-hidden="true"><span style="width:${t.quantity * 100 / maxQty}%"></span></div>
              </div>
            </li>
          </c:forEach>
        </ol>
      </c:otherwise>
    </c:choose>
  </section>

  <aside class="aside card card--flush" aria-labelledby="ro-title">
    <div class="row-between" style="padding:16px 16px 8px"><h2 class="t-title2" id="ro-title">최근 주문</h2><a class="text-btn" href="${ctx}/admin/orders">전체 보기</a></div>
    <c:choose>
      <c:when test="${empty recentOrders}"><p class="t-body3 muted" style="padding:8px 16px 20px">아직 주문이 없어요</p></c:when>
      <c:otherwise>
        <c:forEach var="o" items="${recentOrders}">
          <a class="list-row" href="${ctx}/orders/receipt?id=${o.orderId}">
            <div class="list-row__body">
              <p class="t-label" style="color:var(--text-strong)"><c:out value="${o.memberName}" /> · <c:out value="${o.firstItemName}" /><c:if test="${o.itemCount > 1}"> 외 ${o.itemCount - 1}종</c:if></p>
              <p class="meta"><span class="num">${o.orderedAtText}</span></p>
            </div>
            <c:choose>
              <c:when test="${o.cancelled}"><span class="pill pill--danger">취소됨</span></c:when>
              <c:otherwise><span class="dash-order__end"><span class="price num"><fmt:formatNumber value="${o.payAmount}" pattern="#,##0" />원</span><span class="pill pill--s ${o.statusPill}">${o.statusLabel}</span></span></c:otherwise>
            </c:choose>
          </a>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </aside>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
