package com.quackori.oriweb.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.attachment.AttachmentService;
import com.quackori.oriweb.comment.CommentService;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.dto.PostRequest;
import com.quackori.oriweb.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	/** Post numbers are shown as Roman numerals (max MMMCMXCIX = 3999), so the number of posts is capped at 3999. */
	public static final long MAX_POSTS = 3999;

	private final PostRepository postRepository;
	private final AttachmentService attachmentService;
	private final CommentService commentService;

	public Page<Post> getPosts(Pageable pageable) {
		return postRepository.findAll(pageable);
	}

	public Post getPost(Long id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
	}

	@Transactional
	public Post create(PostRequest request, User loginUser) {
		if (postRepository.count() >= MAX_POSTS) {
			throw new ApiException(HttpStatus.CONFLICT, "게시글은 최대 " + MAX_POSTS + "개까지 작성할 수 있습니다.");
		}
		return postRepository.save(new Post(request.title(), request.content(), loginUser));
	}

	@Transactional
	public Post update(Long id, PostRequest request, User loginUser) {
		Post post = getOwnedPost(id, loginUser);
		post.update(request.title(), request.content());
		// Flush now so the updated timestamp (@UpdateTimestamp) is included in the response
		postRepository.flush();
		return post;
	}

	@Transactional
	public void delete(Long id, User loginUser) {
		Post post = getPost(id);
		// Delete: author or admin (edit: author only)
		if (!post.isWrittenBy(loginUser) && !loginUser.isAdmin()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자 또는 관리자만 삭제할 수 있습니다.");
		}
		commentService.deleteAllByPost(post);
		attachmentService.deleteAllByPost(post);
		postRepository.delete(post);
	}

	/** Returns the post, or throws 403 if the user is not its author. */
	public Post getOwnedPost(Long id, User loginUser) {
		Post post = getPost(id);
		if (!post.isWrittenBy(loginUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자만 가능합니다.");
		}
		return post;
	}

}
