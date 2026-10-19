package com.quackori.oriweb.user;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String username;

	/** For OAuth users, a hash of a random value is stored so password login is impossible. */
	@Column(nullable = false)
	private String password;

	/** OAuth provider (e.g. github). Null for users who signed up with a password. */
	@Column(length = 20)
	private String provider;

	/** Unique user ID from the OAuth provider. Null for users who signed up with a password. */
	@Column(name = "provider_id", length = 100)
	private String providerId;

	/** Role. Null for users created before this column existed; treated as USER. */
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Role role = Role.USER;

	/** Whether the account is suspended. Null for users created before this column existed; treated as not suspended. */
	private Boolean suspended = false;

	/** 로그인 연속 실패 횟수. 임계치 도달 시 계정이 일정 시간 잠긴다. */
	@ColumnDefault("0")
	@Column(nullable = false)
	private int failedLoginCount = 0;

	/** 로그인 잠금 해제 시각. null이면 잠금 상태가 아니다. */
	private LocalDateTime lockedUntil;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public boolean isAdmin() {
		return role == Role.ADMIN;
	}

	public boolean isSuspended() {
		return Boolean.TRUE.equals(suspended);
	}

	public void changeRole(Role role) {
		this.role = role;
	}

	public void changeSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public boolean isLoginLocked() {
		return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
	}

	/** 로그인 실패를 기록하고, 임계치 도달 시 일정 시간 계정을 잠근다. */
	public void recordFailedLogin(int maxAttempts, Duration lockDuration) {
		if (lockedUntil != null && !isLoginLocked()) {
			// 이전 잠금이 만료되었으면 새로 집계한다
			failedLoginCount = 0;
			lockedUntil = null;
		}
		failedLoginCount++;
		if (failedLoginCount >= maxAttempts) {
			lockedUntil = LocalDateTime.now().plus(lockDuration);
		}
	}

	public void resetFailedLogin() {
		failedLoginCount = 0;
		lockedUntil = null;
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public static User ofOAuth(String username, String password, String provider, String providerId) {
		User user = new User(username, password);
		user.provider = provider;
		user.providerId = providerId;
		return user;
	}

	public static User ofAdmin(String username, String password) {
		User user = new User(username, password);
		user.role = Role.ADMIN;
		return user;
	}

}
