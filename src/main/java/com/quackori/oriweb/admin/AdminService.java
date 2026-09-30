package com.quackori.oriweb.admin;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.user.Role;
import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

	private final UserRepository userRepository;

	public List<User> getUsers() {
		return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	@Transactional
	public User changeRole(Long userId, Role role, User admin) {
		User target = getOtherUser(userId, admin, "자기 자신의 권한은 변경할 수 없습니다.");
		target.changeRole(role);
		return target;
	}

	@Transactional
	public User changeSuspended(Long userId, boolean suspended, User admin) {
		User target = getOtherUser(userId, admin, "자기 자신은 정지할 수 없습니다.");
		target.changeSuspended(suspended);
		return target;
	}

	/** Admins cannot target themselves, to avoid ending up with no admins. */
	private User getOtherUser(Long userId, User admin, String selfMessage) {
		if (admin.getId().equals(userId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, selfMessage);
		}
		return userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
	}

}
