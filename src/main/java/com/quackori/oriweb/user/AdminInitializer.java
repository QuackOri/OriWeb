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
 * 앱 시작 시 관리자 계정을 만든다.
 * ADMIN_USERNAME, ADMIN_PASSWORD 환경변수(.env)가 모두 있고, 같은 아이디의 계정이 아직 없을 때만 생성한다.
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
			log.info("관리자 계정 설정(ADMIN_USERNAME, ADMIN_PASSWORD)이 없어 관리자 계정을 만들지 않습니다.");
			return;
		}
		if (!username.matches("^[a-zA-Z0-9]{4,20}$") || password.length() < 4) {
			log.warn("관리자 계정 설정이 규칙(아이디 영문/숫자 4~20자, 비밀번호 4자 이상)에 맞지 않아 만들지 않습니다.");
			return;
		}
		userRepository.findByUsername(username).ifPresentOrElse(
				user -> {
					if (!user.isAdmin()) {
						log.warn("'{}'은(는) 이미 일반 사용자 아이디라 관리자로 만들지 않습니다. 다른 아이디를 사용하세요.", username);
					}
				},
				() -> {
					userRepository.save(User.ofAdmin(username, passwordEncoder.encode(password)));
					log.info("관리자 계정 '{}'을(를) 만들었습니다.", username);
				});
	}

}
