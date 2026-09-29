package com.quackori.oriweb.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.quackori.oriweb.auth.oauth.OAuthLoginSuccessHandler;

/**
 * Spring Security는 OAuth 2.0 로그인(GitHub) 처리에만 사용한다.
 * 접근 권한 확인은 기존처럼 각 API에서 세션(SessionConst.LOGIN_USER_ID)으로 직접 한다.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuthLoginSuccessHandler successHandler)
			throws Exception {
		http
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				// 기존 API 동작 유지 (Spring Security 도입 전과 동일)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				// GET /oauth2/authorization/github → GitHub 인증 → /login/oauth2/code/github 콜백
				.oauth2Login(oauth -> oauth
						.loginPage("/login.html")
						.successHandler(successHandler)
						.failureUrl("/login.html?error=oauth"));
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
