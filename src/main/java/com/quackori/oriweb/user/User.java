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
