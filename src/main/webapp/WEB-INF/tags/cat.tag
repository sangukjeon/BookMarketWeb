<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="name" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 장르 태그. 색은 Genre enum의 tone과 같게 (study 파랑 / class 초록 / rules 보라 / tip 주황) --%>
<c:choose>
  <c:when test="${name == 'IT/컴퓨터' or name == '경제/경영'}"><c:set var="k" value="study" /></c:when>
  <c:when test="${name == '인문' or name == '과학' or name == '여행'}"><c:set var="k" value="class" /></c:when>
  <c:when test="${name == '소설' or name == '어린이'}"><c:set var="k" value="rules" /></c:when>
  <c:otherwise><c:set var="k" value="tip" /></c:otherwise>
</c:choose>
<span class="tag tag--${k}"><c:out value="${name}" /></span>
