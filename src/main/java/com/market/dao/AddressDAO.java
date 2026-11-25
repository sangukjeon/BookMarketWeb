package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.market.dto.Address;
import com.market.util.DBUtil;

/**
 * 배송지 여러 개 (member_address). 회원당 최대 5개.
 * 기본 배송지는 member.address·phone에도 같이 써 둬서 주문서가 자동으로 채워진다.
 */
public class AddressDAO {
	public static final int MAX = 5;

	private Address map(ResultSet rs) throws SQLException {
		Address a = new Address();
		a.setAddressId(rs.getLong("address_id"));
		a.setMemberNo(rs.getLong("member_no"));
		a.setLabel(rs.getString("label"));
		a.setReceiverName(rs.getString("receiver_name"));
		a.setPhone(rs.getString("phone"));
		a.setAddress(rs.getString("address"));
		a.setDefaultAddress(rs.getBoolean("is_default"));
		return a;
	}

	/** 기본 배송지가 먼저 */
	public List<Address> findByMember(long memberNo) throws SQLException {
		List<Address> list = new ArrayList<>();
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT * FROM member_address WHERE member_no = ? ORDER BY is_default DESC, address_id")) {
			ps.setLong(1, memberNo);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(map(rs));
			}
		}
		return list;
	}

	public Address find(long memberNo, long addressId) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"SELECT * FROM member_address WHERE member_no = ? AND address_id = ?")) {
			ps.setLong(1, memberNo);
			ps.setLong(2, addressId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	/** 추가. 첫 배송지이거나 makeDefault면 기본 배송지로. 5개가 차 있으면 false */
	public boolean add(Address a, boolean makeDefault) throws SQLException {
		List<Address> mine = findByMember(a.getMemberNo());
		if (mine.size() >= MAX)
			return false;
		long id;
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO member_address (member_no, label, receiver_name, phone, address, is_default, created_at) VALUES (?,?,?,?,?,FALSE,?)",
				new String[] { "address_id" })) {
			ps.setLong(1, a.getMemberNo());
			ps.setString(2, a.getLabel());
			ps.setString(3, a.getReceiverName());
			ps.setString(4, a.getPhone());
			ps.setString(5, a.getAddress());
			ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Seoul")).withNano(0)));
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				rs.next();
				id = rs.getLong(1);
			}
		}
		if (mine.isEmpty() || makeDefault)
			setDefault(a.getMemberNo(), id);
		return true;
	}

	/** 기본 배송지 바꾸기 + member 테이블의 기본 주소·연락처도 같이 */
	public void setDefault(long memberNo, long addressId) throws SQLException {
		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement ps = con.prepareStatement(
						"UPDATE member_address SET is_default = (address_id = ?) WHERE member_no = ?")) {
					ps.setLong(1, addressId);
					ps.setLong(2, memberNo);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement(
						"UPDATE member SET address = (SELECT a.address FROM member_address a WHERE a.address_id = ? AND a.member_no = ?), "
								+ "phone = COALESCE((SELECT a.phone FROM member_address a WHERE a.address_id = ? AND a.member_no = ?), phone) WHERE member_no = ?")) {
					ps.setLong(1, addressId);
					ps.setLong(2, memberNo);
					ps.setLong(3, addressId);
					ps.setLong(4, memberNo);
					ps.setLong(5, memberNo);
					ps.executeUpdate();
				}
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
	}

	/** 지우기. 기본 배송지를 지우면 남은 것 중 첫 번째가 기본이 된다 */
	public void delete(long memberNo, long addressId) throws SQLException {
		Address a = find(memberNo, addressId);
		if (a == null)
			return;
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"DELETE FROM member_address WHERE member_no = ? AND address_id = ?")) {
			ps.setLong(1, memberNo);
			ps.setLong(2, addressId);
			ps.executeUpdate();
		}
		if (a.isDefaultAddress()) {
			List<Address> rest = findByMember(memberNo);
			if (!rest.isEmpty())
				setDefault(memberNo, rest.get(0).getAddressId());
		}
	}
}
