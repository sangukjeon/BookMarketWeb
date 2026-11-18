package com.market.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 주문(영수증) — orders 테이블 + 주문 항목 목록 */
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;
	private long orderId;
	private long memberNo;
	private String receiverName;
	private String receiverPhone;
	private String address;
	private int itemTotal;
	private int discount;
	private int shippingFee;
	private int payAmount;
	private String couponCode;
	private LocalDateTime orderedAt;
	private String status = "ORDERED";
	private LocalDateTime cancelledAt;
	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;
	private String memberName;
	private int itemCount;
	private String firstItemName;
	private List<OrderItem> items = new ArrayList<>();

	/** 화면에 보이는 주문번호: BM-20261111-000012 */
	public String getOrderNo() {
		String d = orderedAt == null ? "" : orderedAt.format(DateTimeFormatter.BASIC_ISO_DATE);
		return String.format("BM-%s-%06d", d, orderId);
	}

	public String getOrderedAtText() {
		return orderedAt == null ? "" : orderedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
	}

	public boolean isCancelled() { return "CANCELLED".equals(status); }

	// ---------- 배송 단계: ORDERED → PREPARING → SHIPPING → DELIVERED (그리고 CANCELLED) ----------
	public static final String[] FLOW = { "ORDERED", "PREPARING", "SHIPPING", "DELIVERED" };

	/** 취소는 배송이 시작되기 전(주문완료·배송 준비 중)까지만 */
	public boolean isCancellable() { return "ORDERED".equals(status) || "PREPARING".equals(status); }

	/** 진행 막대용 단계 번호 1~4 (취소는 0) */
	public int getStep() {
		for (int i = 0; i < FLOW.length; i++)
			if (FLOW[i].equals(status))
				return i + 1;
		return 0;
	}

	/** 다음 단계 (마지막이거나 취소됐으면 null) */
	public String getNextStatus() { return nextOf(status); }

	public static String nextOf(String status) {
		for (int i = 0; i < FLOW.length - 1; i++)
			if (FLOW[i].equals(status))
				return FLOW[i + 1];
		return null;
	}

	public static String labelOf(String status) {
		switch (status == null ? "" : status) {
		case "ORDERED": return "주문완료";
		case "PREPARING": return "배송 준비 중";
		case "SHIPPING": return "배송 중";
		case "DELIVERED": return "배송 완료";
		case "CANCELLED": return "취소됨";
		default: return status;
		}
	}

	public String getStatusLabel() { return labelOf(status); }

	/** 상태 배지 색 */
	public String getStatusPill() {
		switch (status) {
		case "PREPARING": return "pill--warn";
		case "SHIPPING": return "pill--info";
		case "DELIVERED": return "pill--success";
		case "CANCELLED": return "pill--danger";
		default: return "pill--neutral";
		}
	}

	/** 관리자 버튼 이름: 다음 단계로 넘기기 */
	public String getNextActionLabel() {
		String n = getNextStatus();
		if (n == null) return null;
		switch (n) {
		case "PREPARING": return "배송 준비하기";
		case "SHIPPING": return "배송 시작하기";
		default: return "배송 완료하기";
		}
	}

	public String getShippedAtText() {
		return shippedAt == null ? "" : shippedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
	}
	public String getDeliveredAtText() {
		return deliveredAt == null ? "" : deliveredAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
	}
	public LocalDateTime getShippedAt() { return shippedAt; }
	public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
	public LocalDateTime getDeliveredAt() { return deliveredAt; }
	public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
	public String getCancelledAtText() {
		return cancelledAt == null ? "" : cancelledAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
	}
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getCancelledAt() { return cancelledAt; }
	public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
	public String getMemberName() { return memberName; }
	public void setMemberName(String memberName) { this.memberName = memberName; }

	public long getOrderId() { return orderId; }
	public void setOrderId(long orderId) { this.orderId = orderId; }
	public long getMemberNo() { return memberNo; }
	public void setMemberNo(long memberNo) { this.memberNo = memberNo; }
	public String getReceiverName() { return receiverName; }
	public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
	public String getReceiverPhone() { return receiverPhone; }
	public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public int getItemTotal() { return itemTotal; }
	public void setItemTotal(int itemTotal) { this.itemTotal = itemTotal; }
	public int getDiscount() { return discount; }
	public void setDiscount(int discount) { this.discount = discount; }
	public int getShippingFee() { return shippingFee; }
	public void setShippingFee(int shippingFee) { this.shippingFee = shippingFee; }
	public int getPayAmount() { return payAmount; }
	public void setPayAmount(int payAmount) { this.payAmount = payAmount; }
	public String getCouponCode() { return couponCode; }
	public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
	public LocalDateTime getOrderedAt() { return orderedAt; }
	public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
	public int getItemCount() { return itemCount; }
	public void setItemCount(int itemCount) { this.itemCount = itemCount; }
	public String getFirstItemName() { return firstItemName; }
	public void setFirstItemName(String firstItemName) { this.firstItemName = firstItemName; }
	public List<OrderItem> getItems() { return items; }
	public void setItems(List<OrderItem> items) { this.items = items; }
}
