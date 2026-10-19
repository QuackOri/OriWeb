package com.quackori.oriweb.common;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 동일 클라이언트(IP)의 과도한 API 요청을 제한한다(고정 윈도우 방식).
 * 임계치/윈도우는 보안정책으로 정의할 수 있도록 설정값(app.rate-limit.*)으로 외부화한다.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final int maxRequests;
	private final long windowMillis;
	private final Map<String, Counter> counters = new ConcurrentHashMap<>();

	public RateLimitFilter(
			@Value("${app.rate-limit.max-requests:100}") int maxRequests,
			@Value("${app.rate-limit.window-seconds:60}") long windowSeconds) {
		this.maxRequests = maxRequests;
		this.windowMillis = windowSeconds * 1000;
	}

	// API 요청에만 적용한다(정적 리소스 제외)
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (isOverLimit(request.getRemoteAddr())) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(
					"{\"status\":429,\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean isOverLimit(String clientIp) {
		long now = System.currentTimeMillis();
		Counter counter = counters.compute(clientIp, (key, existing) -> {
			if (existing == null || now - existing.windowStart >= windowMillis) {
				return new Counter(now);
			}
			existing.count++;
			return existing;
		});
		return counter.count > maxRequests;
	}

	private static class Counter {
		final long windowStart;
		int count = 1;

		Counter(long windowStart) {
			this.windowStart = windowStart;
		}
	}

}
