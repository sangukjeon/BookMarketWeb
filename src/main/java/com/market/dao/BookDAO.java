package com.market.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.market.api.BookPage;
import com.market.dto.Book;
import com.market.util.DBUtil;

/** book 테이블 (원본 BookInIt의 book.txt 읽기를 대신한다) */
public class BookDAO {
	/** 취소되지 않은 주문에서 팔린 권수 */
	private static final String SOLD = "(SELECT COALESCE(SUM(i.quantity), 0) FROM order_item i JOIN orders o ON o.order_id = i.order_id "
			+ "WHERE i.book_id = b.book_id AND o.status = 'ORDERED')";

	private Book map(ResultSet rs) throws SQLException {
		Book b = new Book(rs.getString("book_id"), rs.getString("name"), rs.getInt("unit_price"), rs.getString("author"),
				rs.getString("description"), rs.getString("category"), rs.getString("release_date"),
				rs.getString("image"));
		b.setPublisher(rs.getString("publisher"));
		b.setSource(rs.getString("source"));
		return b;
	}

	/**
	 * DB에서 목록·검색 (베스트셀러·신간 탭, 카카오 키가 없을 때의 검색)
	 * 검색어가 없으면 tab(bestseller=판매량 순 / new=출판일 순), 있으면 sort(accuracy/latest/sales)
	 */
	public BookPage browse(String tab, String keyword, String sort, int page, int size) throws SQLException {
		boolean searching = keyword != null && !keyword.isBlank();
		String where = searching ? " WHERE LOWER(b.name) LIKE ? OR LOWER(b.author) LIKE ? OR LOWER(b.publisher) LIKE ?" : "";
		String order;
		if (searching) {
			order = switch (sort == null ? "" : sort) {
			case "latest" -> " ORDER BY b.release_date DESC";
			case "sales" -> " ORDER BY sold DESC, b.release_date DESC";
			// 정확도: 제목이 검색어로 시작하면 먼저, 그다음 제목에 들어 있으면
			default -> " ORDER BY (LOWER(b.name) LIKE ?) DESC, (LOWER(b.name) LIKE ?) DESC, b.name";
			};
		} else {
			order = "new".equals(tab) ? " ORDER BY b.release_date DESC" : " ORDER BY sold DESC, b.release_date DESC";
		}
		// 대소문자 구분 없이 찾기: PostgreSQL의 LIKE는 대소문자를 구분해서 양쪽을 소문자로 맞춘다
		String kw = searching ? keyword.trim().toLowerCase() : null;
		String like = searching ? "%" + kw + "%" : null;

		BookPage result = new BookPage();
		result.setPage(page);
		try (Connection con = DBUtil.getConnection()) {
			try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM book b" + where)) {
				if (searching)
					for (int i = 1; i <= 3; i++)
						ps.setString(i, like);
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					result.setTotal(rs.getInt(1));
				}
			}
			result.setTotalPages((result.getTotal() + size - 1) / size);
			String sql = "SELECT b.*, " + SOLD + " AS sold FROM book b" + where + order + " LIMIT ? OFFSET ?";
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				int n = 1;
				if (searching) {
					for (int i = 0; i < 3; i++)
						ps.setString(n++, like);
					if (order.contains("LIKE ?")) {
						ps.setString(n++, kw + "%");
						ps.setString(n++, like);
					}
				}
				ps.setInt(n++, size);
				ps.setInt(n, (page - 1) * size);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						result.getBooks().add(map(rs));
				}
			}
		}
		return result;
	}

	/** 관리자 목록: 전체 */
	public List<Book> findAll() throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT * FROM book ORDER BY source, release_date DESC");
				ResultSet rs = ps.executeQuery()) {
			List<Book> list = new ArrayList<>();
			while (rs.next())
				list.add(map(rs));
			return list;
		}
	}

	public Book findById(String bookId) throws SQLException {
		if (bookId == null)
			return null;
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE book_id = ?")) {
			ps.setString(1, bookId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	/**
	 * 외부 API(카카오)에서 받은 책을 저장한다. 처음 보는 책은 추가, 이미 있는 API 책은 가격·표지 등을 갱신.
	 * 관리자가 직접 등록한 책(LOCAL)은 덮어쓰지 않는다.
	 */
	public void saveFromApi(List<Book> books) throws SQLException {
		if (books.isEmpty())
			return;
		try (Connection con = DBUtil.getConnection();
				PreparedStatement find = con.prepareStatement("SELECT source FROM book WHERE book_id = ?");
				PreparedStatement ins = con.prepareStatement(
						"INSERT INTO book (book_id, name, unit_price, author, description, category, release_date, image, publisher, source) VALUES (?,?,?,?,?,?,?,?,?,?)");
				PreparedStatement upd = con.prepareStatement(
						"UPDATE book SET name=?, unit_price=?, author=?, description=?, category=?, release_date=?, image=?, publisher=? WHERE book_id=? AND source <> 'LOCAL'")) {
			for (Book b : books) {
				find.setString(1, b.getBookId());
				boolean exists;
				try (ResultSet rs = find.executeQuery()) {
					exists = rs.next();
				}
				if (!exists) {
					bind(ins, b);
					ins.setString(9, b.getPublisher());
					ins.setString(10, b.getSource());
					ins.executeUpdate();
				} else {
					upd.setString(1, b.getName());
					upd.setInt(2, b.getUnitPrice());
					upd.setString(3, b.getAuthor());
					upd.setString(4, b.getDescription());
					upd.setString(5, b.getCategory()); // 카카오는 분야를 주지 않아 null
					upd.setString(6, b.getReleaseDate());
					upd.setString(7, b.getImage());
					upd.setString(8, b.getPublisher());
					upd.setString(9, b.getBookId());
					upd.executeUpdate();
				}
			}
		}
	}

	public void insert(Book b) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"INSERT INTO book (book_id, name, unit_price, author, description, category, release_date, image, publisher, source) VALUES (?,?,?,?,?,?,?,?,?,'LOCAL')")) {
			bind(ps, b);
			ps.setString(9, b.getPublisher());
			ps.executeUpdate();
		}
	}

	public void update(Book b) throws SQLException {
		try (Connection con = DBUtil.getConnection(); PreparedStatement ps = con.prepareStatement(
				"UPDATE book SET name=?, unit_price=?, author=?, description=?, category=?, release_date=?, image=?, publisher=? WHERE book_id=?")) {
			ps.setString(1, b.getName());
			ps.setInt(2, b.getUnitPrice());
			ps.setString(3, b.getAuthor());
			ps.setString(4, b.getDescription());
			ps.setString(5, b.getCategory());
			ps.setString(6, b.getReleaseDate());
			ps.setString(7, b.getImage());
			ps.setString(8, b.getPublisher());
			ps.setString(9, b.getBookId());
			ps.executeUpdate();
		}
	}

	/** 주문 기록이 있는 책은 외래키 때문에 지울 수 없다 → false */
	public boolean delete(String bookId) throws SQLException {
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM book WHERE book_id = ?")) {
			ps.setString(1, bookId);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			if (DBErrors.isConstraintViolation(e))
				return false; // 주문 기록이 참조하고 있음 (외래키)
			throw e;
		}
	}

	private void bind(PreparedStatement ps, Book b) throws SQLException {
		ps.setString(1, b.getBookId());
		ps.setString(2, b.getName());
		ps.setInt(3, b.getUnitPrice());
		ps.setString(4, b.getAuthor());
		ps.setString(5, b.getDescription());
		ps.setString(6, b.getCategory());
		ps.setString(7, b.getReleaseDate());
		ps.setString(8, b.getImage());
	}
}
