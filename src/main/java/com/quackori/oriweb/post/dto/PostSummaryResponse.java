package com.quackori.oriweb.post.dto;

import java.time.LocalDateTime;

import com.quackori.oriweb.post.Post;

/** Post list item (without content) */
public record PostSummaryResponse(
		Long id,
		String title,
		String authorUsername,
		LocalDateTime createdAt) {

	public static PostSummaryResponse from(Post post) {
		return new PostSummaryResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getUsername(),
				post.getCreatedAt());
	}

}
