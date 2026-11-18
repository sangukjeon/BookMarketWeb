<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="name" required="true" %>
<%-- 소셜 로그인 버튼용 공식 심볼. 각 회사 버튼 가이드에 따라 로고 모양·색을 바꾸지 않는다 --%>
<% if ("kakao".equals(name)) { %>
<svg class="brand-logo" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path fill="#000" d="M12 3.2C6.6 3.2 2.2 6.66 2.2 10.93c0 2.76 1.83 5.18 4.6 6.55l-.94 3.45c-.08.31.27.55.53.38l4.07-2.69c.5.05 1.02.08 1.54.08 5.4 0 9.8-3.46 9.8-7.77S17.4 3.2 12 3.2z"/></svg>
<% } else if ("naver".equals(name)) { %>
<svg class="brand-logo" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path fill="#fff" d="M15.56 12.7 8.17 2H2v20h6.44V11.3L15.83 22H22V2h-6.44z" transform="translate(2.4 2.4) scale(.8)"/></svg>
<% } else if ("google".equals(name)) { %>
<svg class="brand-logo" viewBox="0 0 48 48" aria-hidden="true" focusable="false">
<path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
<path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
<path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
<path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
</svg>
<% } %>
