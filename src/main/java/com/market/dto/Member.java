package com.market.dto;

/** 회원 (Person 상속). 원본의 User/Admin을 role 값으로 합쳤다. */
public class Member extends Person {
	private static final long serialVersionUID = 1L;
	private long memberNo;
	private String loginId;
	private String passwordHash;
	private String salt;
	private String email;
	private String provider = "LOCAL";
	private String providerId;
	private String role = "USER";
	private String status = "ACTIVE";
	private java.time.LocalDateTime createdAt;
	// 관리자 회원 목록에서만 채운다 (취소되지 않은 주문 기준)
	private int orderCount;
	private long totalSpent;

	public boolean isAdmin() { return "ADMIN".equals(role); }
	public boolean isSocial() { return !"LOCAL".equals(provider); }
	public boolean isSuspended() { return "SUSPENDED".equals(status); }

	/** 화면용 가입 경로 이름 */
	public String getProviderLabel() {
		switch (provider) {
		case "GOOGLE": return "구글";
		case "NAVER": return "네이버";
		case "KAKAO": return "카카오";
		default: return "전상욱 마켓";
		}
	}

	public long getMemberNo() { return memberNo; }
	public void setMemberNo(long memberNo) { this.memberNo = memberNo; }
	public String getLoginId() { return loginId; }
	public void setLoginId(String loginId) { this.loginId = loginId; }
	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public String getSalt() { return salt; }
	public void setSalt(String salt) { this.salt = salt; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getProvider() { return provider; }
	public void setProvider(String provider) { this.provider = provider; }
	public String getProviderId() { return providerId; }
	public void setProviderId(String providerId) { this.providerId = providerId; }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public java.time.LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
	public String getCreatedAtText() {
		return createdAt == null ? "" : createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"));
	}
	public int getOrderCount() { return orderCount; }
	public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
	public long getTotalSpent() { return totalSpent; }
	public void setTotalSpent(long totalSpent) { this.totalSpent = totalSpent; }
}
