package com.quackori.oriweb.auth.oauth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.quackori.oriweb.auth.SessionConst;
import com.quackori.oriweb.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Called after GitHub authentication succeeds.
 * Links the GitHub account to our user, then stores the user ID in the session like a regular login.
 */
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final OAuthUserService oAuthUserService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
		OAuth2User oAuth2User = token.getPrincipal();

		// GitHub user attributes: id (unique number), login (username)
		String provider = token.getAuthorizedClientRegistrationId();
		String providerId = String.valueOf(oAuth2User.getAttributes().get("id"));
		String login = String.valueOf(oAuth2User.getAttributes().get("login"));

		User user = oAuthUserService.loginOrSignup(provider, providerId, login);

		// Do not log in suspended accounts
		if (user.isSuspended()) {
			request.getSession().invalidate();
			response.sendRedirect("/login.html?error=suspended");
			return;
		}

		// Spring Security already issued a new session ID on successful authentication (session fixation protection)
		request.getSession().setAttribute(SessionConst.LOGIN_USER_ID, user.getId());
		response.sendRedirect("/");
	}

}
