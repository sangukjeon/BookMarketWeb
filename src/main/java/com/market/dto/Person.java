package com.market.dto;

import java.io.Serializable;

/** 사람 공통 부모 클래스 (원본 com.market.member.Person 유지, phone은 int → String) */
public class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String name;
	protected String phone;
	protected String address;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
}
