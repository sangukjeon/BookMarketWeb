<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="cls" required="false" %>
<svg class="icon ${cls}" aria-hidden="true" focusable="false"><use href="${pageContext.request.contextPath}/images/icons.svg#i-${name}"/></svg>
