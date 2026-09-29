package com.quackori.oriweb.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 예외. 상태 코드와 메시지를 담아 GlobalExceptionHandler에서 응답으로 변환한다.
 */
@Getter
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

}
