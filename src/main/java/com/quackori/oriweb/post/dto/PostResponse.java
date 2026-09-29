package com.quackori.oriweb.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.quackori.oriweb.attachment.Attachment;
import com.quackori.oriweb.attachment.AttachmentResponse;
import com.quackori.oriweb.post.Post;

/** 게시글 상세 응답 (첨부파일 목록 포함) */
public record PostResponse(
		Long id,
		String title,
		String content,
		Long authorId,
		String authorUsername,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<AttachmentResponse> attachments) {

	public static PostResponse from(Post post, List<Attachment> attachments) {
		return new PostResponse(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getId(),
				post.getAuthor().getUsername(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				attachments.stream().map(AttachmentResponse::from).toList());
	}

}
