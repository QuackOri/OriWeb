package com.quackori.oriweb.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
		return ResponseEntity.status(e.getStatus())
				.body(new ErrorResponse(e.getStatus().value(), e.getMessage()));
	}

	// @Valid 검증 실패 시 첫 번째 오류 메시지를 응답
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("입력값이 올바르지 않습니다.");
		return ResponseEntity.badRequest()
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
	}

	// JSON 형식 오류, 인코딩 오류 등 요청 본문을 읽을 수 없을 때
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 형식이 올바르지 않습니다."));
	}

}
