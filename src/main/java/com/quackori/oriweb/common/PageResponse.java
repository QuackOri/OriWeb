package com.quackori.oriweb.common;

import java.util.List;

import org.springframework.data.domain.Page;

/** Common paged response for list APIs. page is zero-based. */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

}
