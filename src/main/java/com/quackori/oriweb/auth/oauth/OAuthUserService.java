package com.quackori.oriweb.auth.oauth;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthUserService {

	private static final int MAX_USERNAME_LENGTH = 20;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * OAuth 제공자의 사용자 ID로 기존 회원을 찾고, 없으면 새로 가입시킨다.
	 *
	 * @param provider   제공자 이름 (예: github)
	 * @param providerId 제공자의 사용자 고유 ID (GitHub 사용자 번호)
	 * @param login      제공자의 로그인 아이디 (GitHub login). 우리 서비스 아이디를 만들 때 사용
	 */
	@Transactional
	public User loginOrSignup(String provider, String providerId, String login) {
		return userRepository.findByProviderAndProviderId(provider, providerId)
				.orElseGet(() -> userRepository.save(User.ofOAuth(
						createUsername(provider, providerId, login),
						// 비밀번호 로그인이 불가능하도록 아무도 모르는 값을 해시해서 저장
						passwordEncoder.encode(UUID.randomUUID().toString()),
						provider,
						providerId)));
	}

	/**
	 * 서비스 아이디 생성. 기본은 "gh_로그인아이디"이고, 20자를 넘거나 이미 사용 중이면 "github_사용자번호"를 쓴다.
	 * 일반 회원가입 아이디는 영문/숫자만 허용하므로 밑줄(_)이 들어간 OAuth 아이디와 겹치지 않는다.
	 */
	private String createUsername(String provider, String providerId, String login) {
		String preferred = "gh_" + login;
		if (preferred.length() <= MAX_USERNAME_LENGTH && !userRepository.existsByUsername(preferred)) {
			return preferred;
		}
		return provider + "_" + providerId;
	}

}
