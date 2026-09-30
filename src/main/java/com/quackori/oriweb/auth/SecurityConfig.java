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
 * Spring Security is used only for OAuth 2.0 login (GitHub).
 * Access control is still done by each API using the session (SessionConst.LOGIN_USER_ID).
 */
@Configuration
public class SecurityConfig {

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
				// GET /oauth2/authorization/github -> GitHub authorization -> /login/oauth2/code/github callback
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
