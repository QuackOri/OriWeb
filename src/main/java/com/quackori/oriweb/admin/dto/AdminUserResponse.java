package com.quackori.oriweb.admin.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.user.Role;
import com.quackori.oriweb.user.User;

/** User list item on the admin page */
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
