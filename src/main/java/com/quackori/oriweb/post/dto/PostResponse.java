package com.quackori.oriweb.post.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.post.Post;

/** 게시글 상세 응답 */
public record PostResponse(
		Long id,
		String title,
		String content,
		Long authorId,
		String authorUsername,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static PostResponse from(Post post) {
		return new PostResponse(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getId(),
				post.getAuthor().getUsername(),
				post.getCreatedAt(),
				post.getUpdatedAt());
	}

}
