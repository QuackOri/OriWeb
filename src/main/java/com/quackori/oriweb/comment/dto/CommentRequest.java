package com.quackori.oriweb.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 작성/수정 요청 */
public record CommentRequest(
		@NotBlank(message = "댓글 내용을 입력해주세요.")
		@Size(max = 1000, message = "댓글은 1000자 이하입니다.")
		String content) {
}
