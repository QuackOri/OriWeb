package com.quackori.oriweb.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.quackori.oriweb.auth.dto.LoginRequest;
import com.quackori.oriweb.auth.dto.SignupRequest;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Test
	void signup_savesUserWhenPasswordIsStrong() {
		given(userRepository.existsByUsername("tester1")).willReturn(false);
		given(passwordEncoder.encode(anyString())).willReturn("hash");
		given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

		User user = authService.signup(new SignupRequest("tester1", "Ori!web92"));

		assertThat(user.getUsername()).isEqualTo("tester1");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void signup_rejectsPasswordContainingUsername() {
		given(userRepository.existsByUsername("duck")).willReturn(false);

		assertThatThrownBy(() -> authService.signup(new SignupRequest("duck", "myDuck12!")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void signup_rejectsSequentialPassword() {
		given(userRepository.existsByUsername("tester1")).willReturn(false);

		assertThatThrownBy(() -> authService.signup(new SignupRequest("tester1", "abcd9!xy")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void signup_rejectsRepeatedPassword() {
		given(userRepository.existsByUsername("tester1")).willReturn(false);

		assertThatThrownBy(() -> authService.signup(new SignupRequest("tester1", "aaaa9!xy")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void login_locksAccountAfterMaxFailures() {
		User user = new User("tester1", "hash");
		given(userRepository.findByUsername("tester1")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("wrong", "hash")).willReturn(false);

		// 임계치만큼 실패하면 계정이 잠긴다
		for (int i = 0; i < AuthService.MAX_FAILED_ATTEMPTS; i++) {
			assertThatThrownBy(() -> authService.login(new LoginRequest("tester1", "wrong")))
					.isInstanceOf(ApiException.class)
					.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
		}
		assertThat(user.isLoginLocked()).isTrue();

		// 잠긴 뒤에는 추가 시도가 429로 차단된다
		assertThatThrownBy(() -> authService.login(new LoginRequest("tester1", "wrong")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
	}

	@Test
	void login_resetsFailCountOnSuccess() {
		User user = new User("tester1", "hash");
		given(userRepository.findByUsername("tester1")).willReturn(Optional.of(user));
		given(passwordEncoder.matches("wrong", "hash")).willReturn(false);
		given(passwordEncoder.matches("right", "hash")).willReturn(true);

		assertThatThrownBy(() -> authService.login(new LoginRequest("tester1", "wrong")))
				.isInstanceOf(ApiException.class);
		assertThat(user.getFailedLoginCount()).isEqualTo(1);

		User result = authService.login(new LoginRequest("tester1", "right"));

		assertThat(result).isSameAs(user);
		assertThat(user.getFailedLoginCount()).isZero();
	}

}
