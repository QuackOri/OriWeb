package com.quackori.oriweb.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.quackori.oriweb.auth.SessionConst;
import com.quackori.oriweb.common.GlobalExceptionHandler;
import com.quackori.oriweb.user.User;

class OAuthSignupControllerTest {

	private final OAuthUserService oAuthUserService = mock(OAuthUserService.class);
	private final PendingOAuthSignup pending = new PendingOAuthSignup("github", "12345", "duck");

	private MockMvc mockMvc;
	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new OAuthSignupController(oAuthUserService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
		session = new MockHttpSession();
	}

	@Test
	void getPending_returnsProposedUsername() throws Exception {
		session.setAttribute(SessionConst.OAUTH_PENDING_SIGNUP, pending);
		given(oAuthUserService.proposeUsername(pending)).willReturn("gh_duck");

		mockMvc.perform(get("/api/auth/oauth-signup").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.login").value("duck"))
				.andExpect(jsonPath("$.username").value("gh_duck"));
	}

	@Test
	void getPending_returns404WithoutPendingSignup() throws Exception {
		mockMvc.perform(get("/api/auth/oauth-signup").session(session))
				.andExpect(status().isNotFound());
	}

	@Test
	void signup_createsUserAndLogsIn() throws Exception {
		session.setAttribute(SessionConst.OAUTH_PENDING_SIGNUP, pending);
		User user = User.ofOAuth("gh_duck", "hash", "github", "12345");
		ReflectionTestUtils.setField(user, "id", 7L);
		given(oAuthUserService.signup(pending)).willReturn(user);

		mockMvc.perform(post("/api/auth/oauth-signup").session(session))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("gh_duck"));

		assertThat(session.getAttribute(SessionConst.LOGIN_USER_ID)).isEqualTo(7L);
		assertThat(session.getAttribute(SessionConst.OAUTH_PENDING_SIGNUP)).isNull();
	}

	@Test
	void signup_returns404WithoutPendingSignup() throws Exception {
		mockMvc.perform(post("/api/auth/oauth-signup").session(session))
				.andExpect(status().isNotFound());

		assertThat(session.getAttribute(SessionConst.LOGIN_USER_ID)).isNull();
	}

	@Test
	void cancel_removesPendingSignupWithoutLogin() throws Exception {
		session.setAttribute(SessionConst.OAUTH_PENDING_SIGNUP, pending);

		mockMvc.perform(delete("/api/auth/oauth-signup").session(session))
				.andExpect(status().isNoContent());

		assertThat(session.getAttribute(SessionConst.OAUTH_PENDING_SIGNUP)).isNull();
		assertThat(session.getAttribute(SessionConst.LOGIN_USER_ID)).isNull();
	}

}
