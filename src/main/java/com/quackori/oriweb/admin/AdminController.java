package com.quackori.oriweb.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quackori.oriweb.admin.dto.AdminUserResponse;
import com.quackori.oriweb.admin.dto.RoleRequest;
import com.quackori.oriweb.admin.dto.SuspensionRequest;
import com.quackori.oriweb.auth.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin", description = "관리자 전용: 사용자 목록 / 권한 변경 / 계정 정지")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;
	private final AuthService authService;

	@Operation(summary = "사용자 목록", description = "관리자만 가능")
	@GetMapping("/users")
	public List<AdminUserResponse> getUsers(HttpSession session) {
		authService.getAdmin(session);
		return adminService.getUsers().stream()
				.map(AdminUserResponse::from)
				.toList();
	}

	@Operation(summary = "권한 변경", description = "관리자만 가능. 자기 자신은 변경할 수 없음")
	@PatchMapping("/users/{id}/role")
	public AdminUserResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request,
			HttpSession session) {
		return AdminUserResponse.from(adminService.changeRole(id, request.role(), authService.getAdmin(session)));
	}

	@Operation(summary = "계정 정지 / 해제", description = "관리자만 가능. 정지된 사용자는 로그인할 수 없고 기존 세션도 끊김")
	@PatchMapping("/users/{id}/suspension")
	public AdminUserResponse changeSuspended(@PathVariable Long id, @Valid @RequestBody SuspensionRequest request,
			HttpSession session) {
		return AdminUserResponse.from(
				adminService.changeSuspended(id, request.suspended(), authService.getAdmin(session)));
	}

}
