package com.quackori.oriweb.auth.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private OAuthUserService oAuthUserService;

	private final PendingOAuthSignup pending = new PendingOAuthSignup("github", "12345", "duck");

	@Test
	void proposeUsername_usesGhPrefixWhenAvailable() {
		given(userRepository.existsByUsername("gh_duck")).willReturn(false);

		assertThat(oAuthUserService.proposeUsername(pending)).isEqualTo("gh_duck");
	}

	@Test
	void proposeUsername_fallsBackToProviderIdWhenTaken() {
		given(userRepository.existsByUsername("gh_duck")).willReturn(true);

		assertThat(oAuthUserService.proposeUsername(pending)).isEqualTo("github_12345");
	}

	@Test
	void proposeUsername_fallsBackToProviderIdWhenTooLong() {
		PendingOAuthSignup longLogin = new PendingOAuthSignup("github", "12345", "averyveryverylonglogin");

		assertThat(oAuthUserService.proposeUsername(longLogin)).isEqualTo("github_12345");
	}

	@Test
	void signup_createsLinkedUser() {
		given(userRepository.findByProviderAndProviderId("github", "12345")).willReturn(Optional.empty());
		given(userRepository.existsByUsername("gh_duck")).willReturn(false);
		given(passwordEncoder.encode(anyString())).willReturn("hash");
		given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

		User user = oAuthUserService.signup(pending);

		assertThat(user.getUsername()).isEqualTo("gh_duck");
		assertThat(user.getProvider()).isEqualTo("github");
		assertThat(user.getProviderId()).isEqualTo("12345");
	}

	@Test
	void signup_returnsExistingUserInsteadOfDuplicating() {
		User existing = User.ofOAuth("gh_duck", "hash", "github", "12345");
		given(userRepository.findByProviderAndProviderId("github", "12345")).willReturn(Optional.of(existing));

		assertThat(oAuthUserService.signup(pending)).isSameAs(existing);
		verify(userRepository, never()).save(any(User.class));
	}

}
