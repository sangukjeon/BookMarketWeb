package com.market.dto;

import java.io.Serializable;

/** 회원이 저장해 둔 배송지 (member_address) */
public class Address implements Serializable {
	private static final long serialVersionUID = 1L;
	private long addressId;
	private long memberNo;
	private String label;
	private String receiverName;
	private String phone;
	private String address;
	private boolean defaultAddress;

	public long getAddressId() { return addressId; }
	public void setAddressId(long addressId) { this.addressId = addressId; }
	public long getMemberNo() { return memberNo; }
	public void setMemberNo(long memberNo) { this.memberNo = memberNo; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public String getReceiverName() { return receiverName; }
	public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public boolean isDefaultAddress() { return defaultAddress; }
	public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
