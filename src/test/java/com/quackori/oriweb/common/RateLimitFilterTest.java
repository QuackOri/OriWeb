package com.quackori.oriweb.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

	private MockHttpServletRequest apiRequest(String ip) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/posts");
		request.setRemoteAddr(ip);
		return request;
	}

	@Test
	void blocksApiRequestsOverLimit() throws Exception {
		RateLimitFilter filter = new RateLimitFilter(2, 60);

		// 임계치(2)까지는 통과
		for (int i = 0; i < 2; i++) {
			MockHttpServletResponse response = new MockHttpServletResponse();
			MockFilterChain chain = new MockFilterChain();
			filter.doFilter(apiRequest("1.1.1.1"), response, chain);
			assertThat(chain.getRequest()).isNotNull();
			assertThat(response.getStatus()).isEqualTo(200);
		}

		// 초과 요청은 429로 차단되고 체인으로 넘어가지 않음
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(apiRequest("1.1.1.1"), response, chain);
		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	void limitsPerClientIpIndependently() throws Exception {
		RateLimitFilter filter = new RateLimitFilter(1, 60);

		MockFilterChain chainA = new MockFilterChain();
		filter.doFilter(apiRequest("1.1.1.1"), new MockHttpServletResponse(), chainA);
		assertThat(chainA.getRequest()).isNotNull();

		// 다른 IP는 영향받지 않음
		MockFilterChain chainB = new MockFilterChain();
		filter.doFilter(apiRequest("2.2.2.2"), new MockHttpServletResponse(), chainB);
		assertThat(chainB.getRequest()).isNotNull();
	}

	@Test
	void doesNotLimitNonApiPaths() throws Exception {
		RateLimitFilter filter = new RateLimitFilter(1, 60);

		for (int i = 0; i < 5; i++) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/css/style.css");
			request.setRemoteAddr("1.1.1.1");
			MockFilterChain chain = new MockFilterChain();
			filter.doFilter(request, new MockHttpServletResponse(), chain);
			assertThat(chain.getRequest()).isNotNull();
		}
	}

}
