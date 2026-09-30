package com.quackori.oriweb.user;

import java.time.LocalDateTime;

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

	/** OAuth 사용자는 비밀번호 로그인을 할 수 없도록 임의 값의 해시가 저장된다. */
	@Column(nullable = false)
	private String password;

	/** OAuth 제공자 (예: github). 일반 회원가입 사용자는 null */
	@Column(length = 20)
	private String provider;

	/** OAuth 제공자의 사용자 고유 ID. 일반 회원가입 사용자는 null */
	@Column(name = "provider_id", length = 100)
	private String providerId;

	/** 권한. 이 컬럼이 추가되기 전에 가입한 사용자는 null이며 USER로 취급한다. */
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Role role = Role.USER;

	/** 계정 정지 여부. 이 컬럼이 추가되기 전에 가입한 사용자는 null이며 정지되지 않은 것으로 취급한다. */
	private Boolean suspended = false;

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
