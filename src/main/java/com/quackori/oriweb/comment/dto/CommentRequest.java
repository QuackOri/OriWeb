package com.quackori.oriweb.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request for creating or updating a comment */
public record CommentRequest(
		@NotBlank(message = "댓글 내용을 입력해주세요.")
		@Size(max = 1000, message = "댓글은 1000자 이하입니다.")
		String content) {
}
