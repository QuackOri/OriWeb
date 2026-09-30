package com.quackori.oriweb.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Business exception carrying an HTTP status and message; converted to a response by GlobalExceptionHandler.
 */
@Getter
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

}
