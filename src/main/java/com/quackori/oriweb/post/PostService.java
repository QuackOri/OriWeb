package com.quackori.oriweb.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.dto.PostRequest;
import com.quackori.oriweb.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;

	public Page<Post> getPosts(Pageable pageable) {
		return postRepository.findAll(pageable);
	}

	public Post getPost(Long id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
	}

	@Transactional
	public Post create(PostRequest request, User loginUser) {
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
		postRepository.delete(getOwnedPost(id, loginUser));
	}

	private Post getOwnedPost(Long id, User loginUser) {
		Post post = getPost(id);
		if (!post.isWrittenBy(loginUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "작성자만 수정/삭제할 수 있습니다.");
		}
		return post;
	}

}
