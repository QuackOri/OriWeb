package com.quackori.oriweb.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.Post;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private AttachmentRepository attachmentRepository;

	@Mock
	private FileStorage fileStorage;

	@InjectMocks
	private AttachmentService attachmentService;

	private final Post post = mock(Post.class);

	// PNG 시그니처(89 50 4E 47)로 시작하는 정상 파일
	private static final byte[] PNG_CONTENT = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

	@Test
	void upload_savesWhenExtensionAndSignatureAreValid() {
		MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", PNG_CONTENT);
		given(fileStorage.save(any())).willReturn("stored-uuid");
		given(attachmentRepository.save(any(Attachment.class))).willAnswer(invocation -> invocation.getArgument(0));

		List<Attachment> saved = attachmentService.upload(post, List.of(file));

		assertThat(saved).hasSize(1);
		verify(attachmentRepository).save(any(Attachment.class));
	}

	@Test
	void upload_rejectsDisallowedExtension() {
		MockMultipartFile file = new MockMultipartFile("files", "shell.jsp", "application/octet-stream",
				"<% out.println(1); %>".getBytes());

		assertThatThrownBy(() -> attachmentService.upload(post, List.of(file)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(fileStorage, never()).save(any());
		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

	@Test
	void upload_rejectsWhenContentDoesNotMatchExtension() {
		// 확장자는 png지만 실제 내용은 스크립트인 위장 파일
		MockMultipartFile file = new MockMultipartFile("files", "fake.png", "image/png",
				"<% out.println(1); %>".getBytes());

		assertThatThrownBy(() -> attachmentService.upload(post, List.of(file)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(fileStorage, never()).save(any());
		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

}
