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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

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

}
