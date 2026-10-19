package com.quackori.oriweb.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.Post;
import com.quackori.oriweb.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

	/** 허용 확장자 화이트리스트 (실행 불가능한 이미지/문서 형식만 허용) */
	private static final Set<String> ALLOWED_EXTENSIONS =
			Set.of("jpg", "jpeg", "png", "gif", "pdf", "hwp", "docx", "xlsx", "txt");

	/** 확장자별 파일 시그니처(매직 바이트). txt 등 시그니처가 없는 형식은 포함하지 않는다. */
	private static final Map<String, byte[]> SIGNATURES = Map.of(
			"jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
			"jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
			"png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47},
			"gif", new byte[] {0x47, 0x49, 0x46},
			"pdf", new byte[] {0x25, 0x50, 0x44, 0x46},
			"docx", new byte[] {0x50, 0x4B, 0x03, 0x04},
			"xlsx", new byte[] {0x50, 0x4B, 0x03, 0x04},
			"hwp", new byte[] {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});

	private final AttachmentRepository attachmentRepository;
	private final FileStorage fileStorage;

	public List<Attachment> getAttachments(Long postId) {
		return attachmentRepository.findByPostIdOrderByIdAsc(postId);
	}

	public Attachment getAttachment(Long id) {
		return attachmentRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
	}

	/** Attaches files to the post. The caller (PostService) checks that the user is the post author. */
	@Transactional
	public List<Attachment> upload(Post post, List<MultipartFile> files) {
		if (files == null || files.stream().allMatch(MultipartFile::isEmpty)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택해주세요.");
		}
		List<MultipartFile> targets = files.stream().filter(file -> !file.isEmpty()).toList();
		// 저장 전에 모든 파일을 먼저 검증하여, 일부만 저장되는 상황을 방지한다
		targets.forEach(this::validateFile);

		List<Attachment> saved = new ArrayList<>();
		for (MultipartFile file : targets) {
			String originalName = StringUtils.hasText(file.getOriginalFilename())
					? file.getOriginalFilename() : "file";
			String storedName = fileStorage.save(file);
			saved.add(attachmentRepository.save(
					new Attachment(post, originalName, storedName, file.getSize(), file.getContentType())));
		}
		return saved;
	}

	/** 업로드 파일을 확장자 화이트리스트와 파일 시그니처(매직 바이트)로 검증한다. */
	private void validateFile(MultipartFile file) {
		String extension = getExtension(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"허용되지 않은 파일 형식입니다. (허용: " + String.join(", ", ALLOWED_EXTENSIONS) + ")");
		}
		if (!matchesSignature(file, extension)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "파일 내용이 확장자와 일치하지 않습니다.");
		}
	}

	private static String getExtension(String filename) {
		if (filename == null) {
			return "";
		}
		int dot = filename.lastIndexOf('.');
		return (dot >= 0) ? filename.substring(dot + 1).toLowerCase() : "";
	}

	/** 파일 앞부분을 읽어 확장자에 해당하는 시그니처와 일치하는지 확인한다. 시그니처가 없는 형식(txt 등)은 통과시킨다. */
	private static boolean matchesSignature(MultipartFile file, String extension) {
		byte[] expected = SIGNATURES.get(extension);
		if (expected == null) {
			return true;
		}
		byte[] header = new byte[expected.length];
		try (InputStream in = file.getInputStream()) {
			int read = in.read(header);
			if (read < expected.length) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		return Arrays.equals(header, expected);
	}

	@Transactional
	public void delete(Long id, User loginUser) {
		Attachment attachment = getAttachment(id);
		if (!attachment.getPost().isWrittenBy(loginUser) && !loginUser.isAdmin()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자 또는 관리자만 첨부파일을 삭제할 수 있습니다.");
		}
		attachmentRepository.delete(attachment);
		fileStorage.delete(attachment.getStoredName());
	}

	/** Deletes all attachments of the post (DB rows and files); used when deleting the post. */
	@Transactional
	public void deleteAllByPost(Post post) {
		List<Attachment> attachments = attachmentRepository.findByPostIdOrderByIdAsc(post.getId());
		attachmentRepository.deleteAll(attachments);
		attachments.forEach(attachment -> fileStorage.delete(attachment.getStoredName()));
	}

}
