package com.quackori.oriweb.admin.dto;

import com.quackori.oriweb.user.Role;

import jakarta.validation.constraints.NotNull;

public record RoleRequest(
		@NotNull(message = "권한을 선택해주세요.")
		Role role) {
}
