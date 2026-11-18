<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="name" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
  <c:when test="${name == 'IT전문서'}"><c:set var="k" value="study" /></c:when>
  <c:when test="${name == '컴퓨터입문'}"><c:set var="k" value="class" /></c:when>
  <c:when test="${name.length() % 2 == 0}"><c:set var="k" value="rules" /></c:when>
  <c:otherwise><c:set var="k" value="tip" /></c:otherwise>
</c:choose>
<span class="tag tag--${k}"><c:out value="${name}" /></span>
