<%@ include file="/WEB-INF/views/common/taglibs.jspf" %>
<c:set var="title" value="${editing ? '도서 고치기' : '새 도서 등록'}" />
<c:set var="nav" value="admin" />
<c:set var="containerCls" value="container--read" />
<c:set var="plate" value="${true}" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<a class="text-btn" href="${ctx}/admin/books" style="margin:-8px 0 8px -4px"><ui:icon name="chevron-left" />도서 관리</a>
<div class="page-head"><h1 class="t-heading"><c:out value="${title}" /></h1></div>
<form method="post" action="${ctx}/admin/books/edit" novalidate>
  <%@ include file="/WEB-INF/views/common/csrf.jspf" %>
  <input type="hidden" name="editing" value="${editing ? 'true' : 'false'}">
  <div class="card">
    <c:if test="${not empty error}">
      <div class="banner banner--danger" role="alert" style="margin-bottom:16px"><ui:icon name="triangle-alert" /><span><c:out value="${error}" /></span></div>
    </c:if>
    <div class="field-grid field-grid--2">
      <div class="field">
        <label for="bookId">ISBN</label>
        <input class="input" id="bookId" name="bookId" value="<c:out value='${book.bookId}'/>" placeholder="9788968481475" ${editing ? 'readonly' : ''} required>
      </div>
      <div class="field">
        <label for="unitPrice">가격 (원)</label>
        <input class="input num" id="unitPrice" name="unitPrice" inputmode="numeric" value="${book.unitPrice > 0 ? book.unitPrice : ''}" placeholder="25000" required>
      </div>
    </div>
    <div class="field" style="margin-top:16px">
      <label for="name">도서명</label>
      <input class="input" id="name" name="name" value="<c:out value='${book.name}'/>" required>
    </div>
    <div class="field-grid field-grid--2" style="margin-top:16px">
      <div class="field">
        <label for="author">저자</label>
        <input class="input" id="author" name="author" value="<c:out value='${book.author}'/>">
      </div>
      <div class="field">
        <label for="category">장르</label>
        <select class="input" id="category" name="category">
          <option value="">정하지 않음</option>
          <c:forEach var="g" items="${genres}"><option value="${g.label}" ${book.category == g.label ? 'selected' : ''}>${g.label}</option></c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="stock">재고</label>
        <input class="input num" id="stock" name="stock" type="number" min="0" max="9999" value="${empty book ? 20 : book.stock}" aria-describedby="stockHelp" required>
        <span class="help" id="stockHelp">0으로 두면 품절로 보여요</span>
      </div>
    </div>
    <div class="field-grid field-grid--2" style="margin-top:16px">
      <div class="field">
        <label for="publisher">출판사</label>
        <input class="input" id="publisher" name="publisher" value="<c:out value='${book.publisher}'/>">
      </div>
      <div class="field">
        <label for="releaseDate">출판일</label>
        <input class="input" id="releaseDate" name="releaseDate" type="date" value="${fn:replace(book.releaseDate, '/', '-')}">
      </div>
    </div>
    <div class="field" style="margin-top:16px">
      <label for="image">표지 <span class="muted">(선택)</span></label>
      <input class="input" id="image" name="image" value="<c:out value='${book.image}'/>" placeholder="https://… 또는 images/books 안의 파일 이름" aria-describedby="imgHelp">
      <span class="help" id="imgHelp">비워 두면 기본 표지가 보여요.</span>
    </div>
    <div class="field" style="margin-top:16px">
      <label for="description">설명</label>
      <textarea class="textarea" id="description" name="description"><c:out value="${book.description}" /></textarea>
    </div>
  </div>
  <div class="plate">
    <div class="plate__inner">
      <a class="btn btn-sub" href="${ctx}/admin/books">취소</a>
      <button class="btn btn-main" type="submit">${editing ? '저장하기' : '등록하기'}</button>
    </div>
  </div>
</form>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
