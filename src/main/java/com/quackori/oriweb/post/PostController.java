package com.quackori.oriweb.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quackori.oriweb.auth.AuthService;
import com.quackori.oriweb.common.PageResponse;
import com.quackori.oriweb.post.dto.PostRequest;
import com.quackori.oriweb.post.dto.PostResponse;
import com.quackori.oriweb.post.dto.PostSummaryResponse;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Post", description = "게시글 목록 / 조회 / 작성 / 수정 / 삭제")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final AuthService authService;

	@Operation(summary = "게시글 목록", description = "최신순, 페이지는 0부터 시작합니다. 예: ?page=0&size=10")
	@GetMapping
	public PageResponse<PostSummaryResponse> getPosts(
			@ParameterObject
			@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponse.from(postService.getPosts(pageable).map(PostSummaryResponse::from));
	}

	@Operation(summary = "게시글 상세")
	@GetMapping("/{id}")
	public PostResponse getPost(@PathVariable Long id) {
		return PostResponse.from(postService.getPost(id));
	}

	@Operation(summary = "게시글 작성", description = "로그인 필요")
	@PostMapping
	public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request, HttpSession session) {
		Post post = postService.create(request, authService.getLoginUser(session));
		return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
	}

	@Operation(summary = "게시글 수정", description = "작성자만 가능")
	@PutMapping("/{id}")
	public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest request, HttpSession session) {
		return PostResponse.from(postService.update(id, request, authService.getLoginUser(session)));
	}

	@Operation(summary = "게시글 삭제", description = "작성자만 가능")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
		postService.delete(id, authService.getLoginUser(session));
		return ResponseEntity.noContent().build();
	}

}
