package com.quackori.oriweb.attachment;

import java.util.ArrayList;
import java.util.List;

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

	private final AttachmentRepository attachmentRepository;
	private final FileStorage fileStorage;

	public List<Attachment> getAttachments(Long postId) {
		return attachmentRepository.findByPostIdOrderByIdAsc(postId);
	}

	public Attachment getAttachment(Long id) {
		return attachmentRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
	}

	/** 게시글에 파일을 첨부한다. 게시글 작성자 확인은 호출하는 쪽(PostService)에서 한다. */
	@Transactional
	public List<Attachment> upload(Post post, List<MultipartFile> files) {
		if (files == null || files.stream().allMatch(MultipartFile::isEmpty)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택해주세요.");
		}
		List<Attachment> saved = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}
			String originalName = StringUtils.hasText(file.getOriginalFilename())
					? file.getOriginalFilename() : "file";
			String storedName = fileStorage.save(file);
			saved.add(attachmentRepository.save(
					new Attachment(post, originalName, storedName, file.getSize(), file.getContentType())));
		}
		return saved;
	}

	@Transactional
	public void delete(Long id, User loginUser) {
		Attachment attachment = getAttachment(id);
		if (!attachment.getPost().isWrittenBy(loginUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자만 첨부파일을 삭제할 수 있습니다.");
		}
		attachmentRepository.delete(attachment);
		fileStorage.delete(attachment.getStoredName());
	}

	/** 게시글 삭제 시 첨부파일(DB + 실제 파일)을 함께 삭제한다. */
	@Transactional
	public void deleteAllByPost(Post post) {
		List<Attachment> attachments = attachmentRepository.findByPostIdOrderByIdAsc(post.getId());
		attachmentRepository.deleteAll(attachments);
		attachments.forEach(attachment -> fileStorage.delete(attachment.getStoredName()));
	}

}
