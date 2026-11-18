<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="도서 관리" />
<c:set var="nav" value="admin" />
<c:set var="tab" value="books" />
<c:set var="containerCls" value="container--wide" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<%@ include file="/WEB-INF/views/admin/tabs.jspf" %>
<p class="t-help" style="margin-bottom:8px">카카오 검색 결과로 본 도서는 자동으로 저장돼요. 주문 기록이 있는 책은 지울 수 없어요.</p>
<div class="row-between" style="margin-bottom:12px">
  <p class="t-help">저장된 도서 <strong class="num" style="color:var(--text-strong)">${fn:length(books)}</strong>권</p>
  <a class="btn btn-tint btn-s" href="${ctx}/admin/books/edit"><ui:icon name="plus" cls="icon-sm" />새 도서 등록하기</a>
</div>
<div class="tbl-wrap">
  <table class="tbl tbl--wide">
    <caption class="sr-only">도서 목록</caption>
    <thead><tr><th scope="col">ISBN</th><th scope="col">도서명</th><th scope="col">저자</th><th scope="col">출처</th><th scope="col" class="r">가격</th><th scope="col" class="r">관리</th></tr></thead>
    <tbody>
      <c:forEach var="b" items="${books}">
        <tr>
          <td class="num nowrap"><c:out value="${b.bookId}" /></td>
          <th scope="row"><c:out value="${b.name}" /></th>
          <td><span class="clip" style="display:block;max-width:180px"><c:out value="${b.author}" /></span></td>
          <td class="nowrap"><span class="tag ${b.source == 'LOCAL' ? 'tag--etc' : 'tag--study'}">${b.source == 'LOCAL' ? '직접 등록' : (b.source == 'KAKAO' ? '카카오' : '알라딘')}</span></td>
          <td class="r nowrap"><fmt:formatNumber value="${b.unitPrice}" pattern="#,##0" />원</td>
          <td class="r nowrap">
            <div class="row" style="justify-content:flex-end">
              <a class="btn btn-sub btn-s" href="${ctx}/admin/books/edit?id=${fn:escapeXml(b.bookId)}">고치기</a>
              <form method="post" action="${ctx}/admin/books/delete" data-confirm="이 도서를 지울까요?" data-confirm-desc="<c:out value='${b.name}'/> — 지운 뒤에는 되돌릴 수 없어요." data-confirm-ok="지우기">
                <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
                <input type="hidden" name="id" value="<c:out value='${b.bookId}'/>">
                <button class="btn btn-sub btn-s" type="submit" style="color:var(--danger-strong)">지우기</button>
              </form>
            </div>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty books}"><tr class="empty-row"><td colspan="6">아직 등록된 도서가 없어요</td></tr></c:if>
    </tbody>
  </table>
</div>
</div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
