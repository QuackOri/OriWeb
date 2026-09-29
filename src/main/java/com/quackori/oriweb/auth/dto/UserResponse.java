package com.quackori.oriweb.auth.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.user.User;

public record UserResponse(Long id, String username, LocalDateTime createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
	}

}
