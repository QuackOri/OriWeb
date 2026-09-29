package com.quackori.oriweb.comment.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.comment.Comment;

public record CommentResponse(
		Long id,
		String content,
		Long authorId,
		String authorUsername,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getContent(),
				comment.getAuthor().getId(),
				comment.getAuthor().getUsername(),
				comment.getCreatedAt(),
				comment.getUpdatedAt());
	}

}
