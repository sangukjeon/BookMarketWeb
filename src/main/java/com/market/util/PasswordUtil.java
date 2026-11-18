package com.market.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 비밀번호 암호화 (SHA-256 + salt + 반복).
 * - 회원마다 다른 salt(16바이트)를 만들어 같은 비밀번호라도 저장값이 달라지게 한다.
 * - 해시를 10,000번 반복해 무차별 대입을 느리게 만든다.
 * - DB에는 원래 비밀번호를 저장하지 않고 salt와 해시만 저장한다.
 */
public final class PasswordUtil {
	private static final int ITERATIONS = 10_000;
	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordUtil() {
	}

	public static String newSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public static String hash(String password, String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] result = (salt + password).getBytes(StandardCharsets.UTF_8);
			for (int i = 0; i < ITERATIONS; i++) {
				md.reset();
				result = md.digest(result);
			}
			return Base64.getEncoder().encodeToString(result);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** 시간 차이로 정보가 새지 않도록 MessageDigest.isEqual로 비교한다. */
	public static boolean matches(String password, String salt, String expectedHash) {
		if (password == null || salt == null || expectedHash == null)
			return false;
		byte[] a = hash(password, salt).getBytes(StandardCharsets.UTF_8);
		byte[] b = expectedHash.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(a, b);
	}

	/** schema.sql에 넣을 관리자 해시를 만들 때 쓴다: java PasswordUtil 비밀번호 salt */
	public static void main(String[] args) {
		System.out.println(hash(args[0], args[1]));
	}
}
