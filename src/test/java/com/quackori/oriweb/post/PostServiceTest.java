package com.quackori.oriweb.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.quackori.oriweb.attachment.AttachmentService;
import com.quackori.oriweb.comment.CommentService;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.dto.PostRequest;
import com.quackori.oriweb.user.User;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@Mock
	private PostRepository postRepository;

	@Mock
	private AttachmentService attachmentService;

	@Mock
	private CommentService commentService;

	@InjectMocks
	private PostService postService;

	private final PostRequest request = new PostRequest("title", "content");
	private final User user = new User("tester", "hash");

	@Test
	void create_savesPostWhenBelowLimit() {
		given(postRepository.count()).willReturn(PostService.MAX_POSTS - 1);
		given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

		Post post = postService.create(request, user);

		assertThat(post.getTitle()).isEqualTo("title");
		verify(postRepository).save(any(Post.class));
	}

	@Test
	void create_rejectsWith409WhenLimitReached() {
		given(postRepository.count()).willReturn(PostService.MAX_POSTS);

		assertThatThrownBy(() -> postService.create(request, user))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT))
				.hasMessageContaining("3999");
		verify(postRepository, never()).save(any(Post.class));
	}

}
