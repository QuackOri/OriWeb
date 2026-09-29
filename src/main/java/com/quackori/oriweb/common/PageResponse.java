package com.quackori.oriweb.common;

import java.util.List;

import org.springframework.data.domain.Page;

/** 목록 API 공통 페이지 응답. page는 0부터 시작한다. */
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
