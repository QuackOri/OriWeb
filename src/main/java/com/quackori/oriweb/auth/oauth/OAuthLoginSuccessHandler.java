package com.quackori.oriweb.auth.oauth;

import java.io.IOException;
import java.util.Optional;

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
 * If the GitHub account is already linked, logs in like a regular login (user ID in the session).
 * Otherwise, redirects to the signup confirmation page (/oauth-signup.html).
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

		Optional<User> linkedUser = oAuthUserService.findUser(provider, providerId);

		// First time with this GitHub account: keep its info in the session and ask the user to confirm the signup
		if (linkedUser.isEmpty()) {
			request.getSession().setAttribute(SessionConst.OAUTH_PENDING_SIGNUP,
					new PendingOAuthSignup(provider, providerId, login));
			response.sendRedirect("/oauth-signup.html");
			return;
		}
		User user = linkedUser.get();

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
