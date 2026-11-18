<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="쿠폰 관리" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="coupons" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<div class="split">
  <div class="tbl-wrap">
    <table class="tbl">
      <caption class="sr-only">쿠폰 목록</caption>
      <thead><tr><th scope="col">쿠폰 번호</th><th scope="col">이름</th><th scope="col">혜택</th><th scope="col" class="r">최소 금액</th><th scope="col">상태</th><th scope="col" class="r">관리</th></tr></thead>
      <tbody>
        <c:forEach var="cp" items="${coupons}">
          <tr>
            <th scope="row" class="num nowrap"><c:out value="${cp.code}" /></th>
            <td><c:out value="${cp.name}" /></td>
            <td class="nowrap">${cp.benefitLabel}<c:if test="${not empty cp.maxDiscount}"><br><span class="t-help">최대 <fmt:formatNumber value="${cp.maxDiscount}" pattern="#,##0" />원</span></c:if></td>
            <td class="r nowrap"><fmt:formatNumber value="${cp.minAmount}" pattern="#,##0" />원</td>
            <td class="nowrap">
              <c:choose>
                <c:when test="${cp.active}"><span class="pill pill--success">쓸 수 있음</span></c:when>
                <c:otherwise><span class="pill pill--danger">멈춤</span></c:otherwise>
              </c:choose>
            </td>
            <td class="r">
              <form method="post" action="${ctx}/admin/coupons">
                <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                <input type="hidden" name="action" value="toggle">
                <input type="hidden" name="code" value="${cp.code}">
                <button class="btn btn-sub btn-s" type="submit">${cp.active ? '멈추기' : '다시 쓰기'}</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty coupons}"><tr class="empty-row"><td colspan="6">아직 만든 쿠폰이 없어요</td></tr></c:if>
      </tbody>
    </table>
  </div>

  <aside class="aside">
    <form class="card" method="post" action="${ctx}/admin/coupons" novalidate>
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <h2 class="t-title2 card-title">새 쿠폰 만들기</h2>
      <c:if test="${not empty error}">
        <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
      </c:if>
      <div class="field">
        <label for="code">쿠폰 번호</label>
        <input class="input" id="code" name="code" value="<c:out value='${form.code}'/>" placeholder="SPRING20" autocapitalize="characters">
      </div>
      <div class="field">
        <label for="cname">이름</label>
        <input class="input" id="cname" name="name" value="<c:out value='${form.name}'/>" placeholder="봄맞이 20% 할인">
      </div>
      <div class="field">
        <span class="label" id="typeLabel">할인 방식</span>
        <div class="segment" role="radiogroup" aria-labelledby="typeLabel">
          <label class="chip"><input type="radio" name="type" value="RATE" ${form.type != 'AMOUNT' ? 'checked' : ''} class="sr-only">정률 (%)</label>
          <label class="chip"><input type="radio" name="type" value="AMOUNT" ${form.type == 'AMOUNT' ? 'checked' : ''} class="sr-only">정액 (원)</label>
        </div>
      </div>
      <div class="field-grid field-grid--2" style="margin-top:16px">
        <div class="field">
          <label for="value">할인 값</label>
          <input class="input num" id="value" name="value" inputmode="numeric" value="${form.value > 0 ? form.value : ''}" placeholder="10">
        </div>
        <div class="field">
          <label for="minAmount">최소 주문 금액</label>
          <input class="input num" id="minAmount" name="minAmount" inputmode="numeric" value="${form.minAmount > 0 ? form.minAmount : ''}" placeholder="0">
        </div>
      </div>
      <div class="field" style="margin-top:16px">
        <label for="maxDiscount">최대 할인액 <span class="muted">(정률만, 선택)</span></label>
        <input class="input num" id="maxDiscount" name="maxDiscount" inputmode="numeric" value="${form.maxDiscount}" placeholder="5000">
      </div>
      <button class="btn btn-tint btn-block" type="submit" style="margin-top:24px">쿠폰 만들기</button>
    </form>
  </aside>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
