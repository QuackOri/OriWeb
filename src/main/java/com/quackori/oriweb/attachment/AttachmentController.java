package com.quackori.oriweb.attachment;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quackori.oriweb.auth.AuthService;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.Post;
import com.quackori.oriweb.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Tag(name = "Attachment", description = "첨부파일 업로드 / 다운로드 / 삭제")
@RestController
@RequiredArgsConstructor
public class AttachmentController {

	private final AttachmentService attachmentService;
	private final PostService postService;
	private final AuthService authService;
	private final FileStorage fileStorage;

	@Operation(summary = "첨부파일 업로드", description = "게시글 작성자만 가능. 여러 파일을 한 번에 올릴 수 있습니다. (파일당 최대 10MB)")
	@PostMapping(value = "/api/posts/{postId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<AttachmentResponse>> upload(
			@PathVariable Long postId,
			@RequestPart("files") List<MultipartFile> files,
			HttpSession session) {
		Post post = postService.getOwnedPost(postId, authService.getLoginUser(session));
		List<AttachmentResponse> response = attachmentService.upload(post, files).stream()
				.map(AttachmentResponse::from)
				.toList();
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "첨부파일 목록")
	@GetMapping("/api/posts/{postId}/attachments")
	public List<AttachmentResponse> getAttachments(@PathVariable Long postId) {
		postService.getPost(postId);
		return attachmentService.getAttachments(postId).stream()
				.map(AttachmentResponse::from)
				.toList();
	}

	@Operation(summary = "첨부파일 다운로드", description = "원래 파일명으로 다운로드됩니다.")
	@GetMapping("/api/attachments/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id) {
		Attachment attachment = attachmentService.getAttachment(id);
		Resource resource = fileStorage.load(attachment.getStoredName());
		if (!resource.exists()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "파일이 존재하지 않습니다.");
		}
		// Use filename*=UTF-8'' so non-ASCII (e.g. Korean) file names are preserved
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentLength(attachment.getSize())
				.body(resource);
	}

	@Operation(summary = "첨부파일 삭제", description = "게시글 작성자만 가능")
	@DeleteMapping("/api/attachments/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
		attachmentService.delete(id, authService.getLoginUser(session));
		return ResponseEntity.noContent().build();
	}

}
