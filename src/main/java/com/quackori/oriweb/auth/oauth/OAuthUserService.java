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
	 * Finds the existing user by the provider's user ID, or signs up a new one.
	 *
	 * @param provider   provider name (e.g. github)
	 * @param providerId unique user ID from the provider (GitHub user number)
	 * @param login      login name from the provider (GitHub login), used to build our username
	 */
	@Transactional
	public User loginOrSignup(String provider, String providerId, String login) {
		return userRepository.findByProviderAndProviderId(provider, providerId)
				.orElseGet(() -> userRepository.save(User.ofOAuth(
						createUsername(provider, providerId, login),
						// Store a hash of an unknown random value so password login is impossible
						passwordEncoder.encode(UUID.randomUUID().toString()),
						provider,
						providerId)));
	}

	/**
	 * Builds our username: "gh_{login}" by default, or "github_{providerId}" if that is over 20 chars or already taken.
	 * Regular signup only allows letters and digits, so it never collides with OAuth usernames containing "_".
	 */
	private String createUsername(String provider, String providerId, String login) {
		String preferred = "gh_" + login;
		if (preferred.length() <= MAX_USERNAME_LENGTH && !userRepository.existsByUsername(preferred)) {
			return preferred;
		}
		return provider + "_" + providerId;
	}

}
