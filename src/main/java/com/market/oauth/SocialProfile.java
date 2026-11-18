package com.market.oauth;

/** 소셜 로그인 제공자에게서 받아 오는 사용자 정보 */
public record SocialProfile(String provider, String providerId, String name, String email) {
}
