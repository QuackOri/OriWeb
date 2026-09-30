package com.quackori.oriweb.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.user.Role;
import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AdminService adminService;

	private User user(long id, String username) {
		User user = new User(username, "hash");
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private User admin(long id) {
		User admin = User.ofAdmin("admin", "hash");
		ReflectionTestUtils.setField(admin, "id", id);
		return admin;
	}

	@Test
	void 다른_사용자의_권한을_변경할_수_있다() {
		User target = user(2L, "tester");
		given(userRepository.findById(2L)).willReturn(Optional.of(target));

		adminService.changeRole(2L, Role.ADMIN, admin(1L));

		assertThat(target.isAdmin()).isTrue();
	}

	@Test
	void 자기_자신의_권한은_변경할_수_없다() {
		assertThatThrownBy(() -> adminService.changeRole(1L, Role.USER, admin(1L)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("자기 자신");
	}

	@Test
	void 다른_사용자를_정지하고_해제할_수_있다() {
		User target = user(2L, "tester");
		given(userRepository.findById(2L)).willReturn(Optional.of(target));

		adminService.changeSuspended(2L, true, admin(1L));
		assertThat(target.isSuspended()).isTrue();

		adminService.changeSuspended(2L, false, admin(1L));
		assertThat(target.isSuspended()).isFalse();
	}

	@Test
	void 자기_자신은_정지할_수_없다() {
		assertThatThrownBy(() -> adminService.changeSuspended(1L, true, admin(1L)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("자기 자신");
	}

}
