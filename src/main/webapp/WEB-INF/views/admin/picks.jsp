<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="추천 도서" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="picks" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<div class="split">
  <div>
    <p class="t-body3 muted" style="margin-bottom:12px">가장 최근에 올린 추천이 서점 첫 화면 맨 위에 곰과 함께 보여요.</p>
    <div class="tbl-wrap">
      <table class="tbl">
        <caption class="sr-only">추천 도서 기록</caption>
        <thead><tr><th scope="col">올린 날</th><th scope="col">도서</th><th scope="col">추천 한마디</th><th scope="col" class="r">관리</th></tr></thead>
        <tbody>
          <c:forEach var="p" items="${picks}" varStatus="st">
            <tr>
              <td class="nowrap num">${p.pickedAtText}<c:if test="${st.first}"><br><span class="pill pill--success pill--s">첫 화면에 보임</span></c:if></td>
              <td><a href="${ctx}/books/detail?id=${fn:escapeXml(p.bookId)}"><c:out value="${empty p.book ? p.bookId : p.book.name}" /></a><br><span class="t-help num"><c:out value="${p.bookId}" /></span></td>
              <td><c:out value="${p.comment}" /></td>
              <td class="r">
                <form method="post" action="${ctx}/admin/picks/delete" data-confirm="이 추천을 내릴까요?" data-confirm-ok="내리기">
                  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                  <input type="hidden" name="id" value="${p.pickId}">
                  <button class="btn btn-sub btn-s" type="submit">내리기</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty picks}"><tr class="empty-row"><td colspan="4">아직 올린 추천이 없어요</td></tr></c:if>
        </tbody>
      </table>
    </div>
  </div>

  <aside class="aside">
    <form class="card" method="post" action="${ctx}/admin/picks" novalidate>
      <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
      <h2 class="t-title2 card-title">추천 올리기</h2>
      <c:if test="${not empty error}">
        <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
      </c:if>
      <div class="field">
        <label for="bookId">도서 ID</label>
        <input class="input num" id="bookId" name="bookId" value="<c:out value='${pickBookId}'/>" placeholder="ISBN1234 또는 ISBN 13자리" autocapitalize="characters" aria-describedby="bookIdHelp">
        <span class="help" id="bookIdHelp">도서 관리 목록이나 상세 주소의 id 값이에요</span>
      </div>
      <div class="field">
        <label for="comment">추천 한마디</label>
        <textarea class="input" id="comment" name="comment" rows="4" maxlength="100" placeholder="예: 처음 웹을 배우는 분께 가장 먼저 권하는 책이에요"><c:out value="${pickComment}" /></textarea>
      </div>
      <button class="btn btn-tint btn-block" type="submit" style="margin-top:16px">첫 화면에 올리기</button>
    </form>
  </aside>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
