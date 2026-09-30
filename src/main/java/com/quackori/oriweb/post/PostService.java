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

	/** 게시글 번호를 로마 숫자(최대 MMMCMXCIX = 3999)로 표시하므로 게시글 수도 3999개로 제한 */
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
		// 수정 시각(@UpdateTimestamp)이 응답에 반영되도록 즉시 반영
		postRepository.flush();
		return post;
	}

	@Transactional
	public void delete(Long id, User loginUser) {
		Post post = getPost(id);
		// 삭제는 작성자 또는 관리자 (수정은 작성자만)
		if (!post.isWrittenBy(loginUser) && !loginUser.isAdmin()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자 또는 관리자만 삭제할 수 있습니다.");
		}
		commentService.deleteAllByPost(post);
		attachmentService.deleteAllByPost(post);
		postRepository.delete(post);
	}

	/** 게시글을 조회하고 작성자가 아니면 403 예외. */
	public Post getOwnedPost(Long id, User loginUser) {
		Post post = getPost(id);
		if (!post.isWrittenBy(loginUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "게시글 작성자만 가능합니다.");
		}
		return post;
	}

}
