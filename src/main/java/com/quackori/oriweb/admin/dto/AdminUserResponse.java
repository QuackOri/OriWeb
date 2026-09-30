package com.quackori.oriweb.admin.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.user.Role;
import com.quackori.oriweb.user.User;

/** 관리자 페이지의 사용자 목록 항목 */
public record AdminUserResponse(
		Long id,
		String username,
		Role role,
		String provider,
		boolean suspended,
		LocalDateTime createdAt) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(
				user.getId(),
				user.getUsername(),
				user.isAdmin() ? Role.ADMIN : Role.USER,
				user.getProvider(),
				user.isSuspended(),
				user.getCreatedAt());
	}

}
