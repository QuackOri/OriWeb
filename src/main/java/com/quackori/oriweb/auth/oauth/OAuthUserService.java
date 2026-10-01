package com.quackori.oriweb.auth.oauth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthUserService {

	private static final int MAX_USERNAME_LENGTH = 20;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/** Finds the user already linked to this provider account. */
	public Optional<User> findUser(String provider, String providerId) {
		return userRepository.findByProviderAndProviderId(provider, providerId);
	}

	/**
	 * Signs up a new user for the pending provider account.
	 * If the account was linked in the meantime, returns the existing user instead of creating a duplicate.
	 * The username is decided here (not when the confirmation page was shown) in case it was taken meanwhile.
	 */
	@Transactional
	public User signup(PendingOAuthSignup pending) {
		return findUser(pending.provider(), pending.providerId())
				.orElseGet(() -> userRepository.save(User.ofOAuth(
						proposeUsername(pending),
						// Store a hash of an unknown random value so password login is impossible
						passwordEncoder.encode(UUID.randomUUID().toString()),
						pending.provider(),
						pending.providerId())));
	}

	/**
	 * Builds our username: "gh_{login}" by default, or "{provider}_{providerId}" if that is over 20 chars or already taken.
	 * Regular signup only allows letters and digits, so it never collides with OAuth usernames containing "_".
	 */
	public String proposeUsername(PendingOAuthSignup pending) {
		String preferred = "gh_" + pending.login();
		if (preferred.length() <= MAX_USERNAME_LENGTH && !userRepository.existsByUsername(preferred)) {
			return preferred;
		}
		return pending.provider() + "_" + pending.providerId();
	}

}
