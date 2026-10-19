package com.quackori.oriweb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank(message = "아이디를 입력해주세요.")
		@Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자입니다.")
		String username,

		@NotBlank(message = "비밀번호를 입력해주세요.")
		@Size(min = 8, max = 50, message = "비밀번호는 8~50자입니다.")
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
				message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
		String password) {
}
