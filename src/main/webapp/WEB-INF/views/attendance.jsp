<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="출석 체크" />
<c:set var="containerCls" value="container--read container--account" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<div class="with-side">
<%@ include file="/WEB-INF/views/common/account-side.jspf" %>
<div class="account-main">
<div class="page-head">
  <h1 class="t-heading">출석 체크</h1>
  <p class="t-body3 muted">하루 한 번 출석해요. 이어서 ${goal}일 출석하면 1,000원 쿠폰을 드려요(한 번만).</p>
</div>

<section class="card attend-hero" aria-labelledby="streak-title">
  <img src="${ctx}/images/mascot/${checkedToday ? 'bear-cheer.png' : 'bear-wave.png'}" alt="" width="104" height="110">
  <div class="attend-hero__body">
    <h2 class="t-title1" id="streak-title"><span class="num">${streak}</span>일째 이어서 출석 중</h2>
    <div class="attend-steps" aria-label="${goal}일 중 ${streak > goal ? goal : streak}일">
      <c:forEach begin="1" end="${goal}" var="i"><span class="attend-step${i <= streak ? ' is-on' : ''}"></span></c:forEach>
    </div>
    <p class="t-help">
      <c:choose>
        <c:when test="${rewarded}">7일 출석 쿠폰을 이미 받았어요. 쿠폰함에서 확인하세요.</c:when>
        <c:when test="${streak >= goal}">축하해요! 쿠폰을 받았어요.</c:when>
        <c:otherwise>쿠폰까지 <span class="num">${goal - streak}</span>일 남았어요. 하루라도 빠지면 처음부터 다시 세요.</c:otherwise>
      </c:choose>
    </p>
  </div>
  <form method="post" action="${ctx}/attendance">
    <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
    <c:choose>
      <c:when test="${checkedToday}"><button class="btn btn-sub" type="button" disabled>오늘 출석 완료</button></c:when>
      <c:otherwise><button class="btn btn-main" type="submit"><ui:icon name="calendar-check" />오늘 출석하기</button></c:otherwise>
    </c:choose>
  </form>
</section>

<section class="card" aria-labelledby="cal-title" style="margin-top:12px">
  <div class="row-between card-title">
    <h2 class="t-title2" id="cal-title">${monthLabel}</h2>
    <span class="t-help">이번 달 <span class="num">${monthCount}</span>일 출석</span>
  </div>
  <div class="cal" role="grid" aria-labelledby="cal-title">
    <div class="cal__row cal__head" role="row"><span role="columnheader">일</span><span role="columnheader">월</span><span role="columnheader">화</span><span role="columnheader">수</span><span role="columnheader">목</span><span role="columnheader">금</span><span role="columnheader">토</span></div>
    <div class="cal__days" role="row">
      <c:forEach var="cell" items="${cells}">
        <c:choose>
          <c:when test="${cell.day == 0}"><span class="cal__cell is-blank" role="gridcell"></span></c:when>
          <c:otherwise><span class="cal__cell${cell.checked ? ' is-checked' : ''}${cell.today ? ' is-today' : ''}" role="gridcell" aria-label="${cell.day}일${cell.checked ? ' 출석함' : ''}"><span class="num">${cell.day}</span><c:if test="${cell.checked}"><ui:icon name="paw-print" cls="icon-sm" /></c:if></span></c:otherwise>
        </c:choose>
      </c:forEach>
    </div>
  </div>
</section>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
