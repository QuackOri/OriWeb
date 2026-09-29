package com.quackori.oriweb.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quackori.oriweb.auth.AuthService;
import com.quackori.oriweb.comment.dto.CommentRequest;
import com.quackori.oriweb.comment.dto.CommentResponse;
import com.quackori.oriweb.post.Post;
import com.quackori.oriweb.post.PostService;
import com.quackori.oriweb.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Comment", description = "댓글 목록 / 작성 / 수정 / 삭제")
@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;
	private final PostService postService;
	private final AuthService authService;

	@Operation(summary = "댓글 목록", description = "오래된 순")
	@GetMapping("/api/posts/{postId}/comments")
	public List<CommentResponse> getComments(@PathVariable Long postId) {
		postService.getPost(postId);
		return commentService.getComments(postId).stream()
				.map(CommentResponse::from)
				.toList();
	}

	@Operation(summary = "댓글 작성", description = "로그인 필요")
	@PostMapping("/api/posts/{postId}/comments")
	public ResponseEntity<CommentResponse> create(
			@PathVariable Long postId,
			@Valid @RequestBody CommentRequest request,
			HttpSession session) {
		User loginUser = authService.getLoginUser(session);
		Post post = postService.getPost(postId);
		Comment comment = commentService.create(post, request, loginUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
	}

	@Operation(summary = "댓글 수정", description = "댓글 작성자만 가능")
	@PutMapping("/api/comments/{id}")
	public CommentResponse update(@PathVariable Long id, @Valid @RequestBody CommentRequest request, HttpSession session) {
		return CommentResponse.from(commentService.update(id, request, authService.getLoginUser(session)));
	}

	@Operation(summary = "댓글 삭제", description = "댓글 작성자만 가능")
	@DeleteMapping("/api/comments/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
		commentService.delete(id, authService.getLoginUser(session));
		return ResponseEntity.noContent().build();
	}

}
