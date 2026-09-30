package com.quackori.oriweb.admin.dto;

import jakarta.validation.constraints.NotNull;

public record SuspensionRequest(
		@NotNull(message = "정지 여부를 입력해주세요.")
		Boolean suspended) {
}
