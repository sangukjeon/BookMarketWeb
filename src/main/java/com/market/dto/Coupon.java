package com.market.dto;

import java.io.Serializable;

/** 쿠폰. RATE는 value%, AMOUNT는 value원 할인 */
public class Coupon implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private String name;
	private String type;
	private int value;
	private int minAmount;
	private Integer maxDiscount;
	private boolean active = true;
	// 쿠폰함·관리자 화면에서만 채운다
	private java.time.LocalDateTime registeredAt;
	private boolean used;
	private int registeredCount;
	private int usedCount;

	/** 상품 합계에 대한 할인액. 조건이 안 맞으면 0 */
	public int discountFor(int itemTotal) {
		if (!active || itemTotal < minAmount)
			return 0;
		int d = "RATE".equals(type) ? itemTotal * value / 100 : value;
		if (maxDiscount != null)
			d = Math.min(d, maxDiscount);
		return Math.min(d, itemTotal);
	}

	public String getBenefitLabel() {
		return "RATE".equals(type) ? value + "% 할인" : String.format("%,d원 할인", value);
	}

	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public int getValue() { return value; }
	public void setValue(int value) { this.value = value; }
	public int getMinAmount() { return minAmount; }
	public void setMinAmount(int minAmount) { this.minAmount = minAmount; }
	public Integer getMaxDiscount() { return maxDiscount; }
	public void setMaxDiscount(Integer maxDiscount) { this.maxDiscount = maxDiscount; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public java.time.LocalDateTime getRegisteredAt() { return registeredAt; }
	public void setRegisteredAt(java.time.LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
	public String getRegisteredAtText() {
		return registeredAt == null ? "" : registeredAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"));
	}
	/** 이 회원이 이미 쓴 쿠폰 (취소되지 않은 주문에 쓰임) */
	public boolean isUsed() { return used; }
	public void setUsed(boolean used) { this.used = used; }
	/** 쿠폰함 상태: 사용 가능 / 사용함 / 멈춤 */
	public String getWalletStatus() { return !active ? "멈춤" : used ? "사용함" : "사용 가능"; }
	public boolean isUsable() { return active && !used; }
	public int getRegisteredCount() { return registeredCount; }
	public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
	public int getUsedCount() { return usedCount; }
	public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
}
