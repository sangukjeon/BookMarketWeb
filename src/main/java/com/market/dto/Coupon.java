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
}
