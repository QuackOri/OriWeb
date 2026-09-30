package com.quackori.oriweb.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Creates the admin account on application startup.
 * Only runs when both ADMIN_USERNAME and ADMIN_PASSWORD (.env) are set and no account with that username exists yet.
 */
@Slf4j
@Component
public class AdminInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${app.admin.username:}") String username,
			@Value("${app.admin.password:}") String password) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.password = password;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			log.info("ADMIN_USERNAME / ADMIN_PASSWORD not set; skipping admin account creation.");
			return;
		}
		if (!username.matches("^[a-zA-Z0-9]{4,20}$") || password.length() < 4) {
			log.warn("Admin settings are invalid (username: 4-20 letters/digits, password: 4+ chars); skipping admin account creation.");
			return;
		}
		userRepository.findByUsername(username).ifPresentOrElse(
				user -> {
					if (!user.isAdmin()) {
						log.warn("'{}' is already used by a regular user; not promoting it to admin. Use a different username.", username);
					}
				},
				() -> {
					userRepository.save(User.ofAdmin(username, passwordEncoder.encode(password)));
					log.info("Created admin account '{}'.", username);
				});
	}

}
