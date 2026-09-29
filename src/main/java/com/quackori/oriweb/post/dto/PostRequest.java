package com.quackori.oriweb.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 작성/수정 요청 */
public record PostRequest(
		@NotBlank(message = "제목을 입력해주세요.")
		@Size(max = 200, message = "제목은 200자 이하입니다.")
		String title,

		@NotBlank(message = "내용을 입력해주세요.")
		String content) {
}
