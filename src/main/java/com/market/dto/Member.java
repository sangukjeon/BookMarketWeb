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

	public boolean isAdmin() { return "ADMIN".equals(role); }
	public boolean isSocial() { return !"LOCAL".equals(provider); }

	/** 화면용 가입 경로 이름 */
	public String getProviderLabel() {
		switch (provider) {
		case "GOOGLE": return "구글";
		case "NAVER": return "네이버";
		case "KAKAO": return "카카오";
		default: return "북마켓";
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
}
