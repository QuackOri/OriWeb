package com.quackori.oriweb;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "서버 동작 확인")
@RestController
public class HealthController {

	@Operation(summary = "서버 상태 확인")
	@GetMapping("/api/health")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

}
