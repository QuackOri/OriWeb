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
 * GitHub 인증 성공 후 호출된다.
 * 우리 DB의 사용자와 연결한 뒤, 일반 로그인과 같은 방식으로 세션에 사용자 ID를 저장한다.
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

		// GitHub 사용자 정보: id(고유 번호), login(아이디)
		String provider = token.getAuthorizedClientRegistrationId();
		String providerId = String.valueOf(oAuth2User.getAttributes().get("id"));
		String login = String.valueOf(oAuth2User.getAttributes().get("login"));

		User user = oAuthUserService.loginOrSignup(provider, providerId, login);

		// 세션 ID는 Spring Security가 인증 성공 시 이미 새로 발급함 (세션 고정 방지)
		request.getSession().setAttribute(SessionConst.LOGIN_USER_ID, user.getId());
		response.sendRedirect("/");
	}

}
