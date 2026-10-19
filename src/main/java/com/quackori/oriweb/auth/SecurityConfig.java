package com.quackori.oriweb.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.quackori.oriweb.auth.oauth.OAuthLoginSuccessHandler;

/**
 * Spring Security is used only for OAuth 2.0 login (GitHub).
 * Access control is still done by each API using the session (SessionConst.LOGIN_USER_ID).
 */
@Configuration
public class SecurityConfig {

	/** 운영 환경(HTTPS 종단)에서만 true로 설정하여 HTTP 접속을 HTTPS로 강제 전환한다. */
	@Value("${app.security.require-https:false}")
	private boolean requireHttps;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuthLoginSuccessHandler successHandler)
			throws Exception {
		http
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				// Keep the existing API behavior (same as before Spring Security was added)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				// HSTS: 브라우저가 이후 접속을 항상 HTTPS로 사용하도록 강제 (HTTPS 응답에만 적용됨)
				.headers(headers -> headers
						.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)))
				// GET /oauth2/authorization/github -> GitHub authorization -> /login/oauth2/code/github callback
				.oauth2Login(oauth -> oauth
						.loginPage("/login.html")
						.successHandler(successHandler)
						.failureUrl("/login.html?error=oauth"));

		// 운영 환경에서는 HTTP 접속을 HTTPS로 강제 전환 (로컬 개발에서는 비활성)
		if (requireHttps) {
			http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
		}
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
