package com.quackori.oriweb.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.comment.dto.CommentRequest;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.post.Post;
import com.quackori.oriweb.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;

	public List<Comment> getComments(Long postId) {
		return commentRepository.findByPostIdOrderByIdAsc(postId);
	}

	@Transactional
	public Comment create(Post post, CommentRequest request, User loginUser) {
		return commentRepository.save(new Comment(post, loginUser, request.content()));
	}

	@Transactional
	public Comment update(Long id, CommentRequest request, User loginUser) {
		Comment comment = getOwnedComment(id, loginUser);
		comment.update(request.content());
		// 수정 시각(@UpdateTimestamp)이 응답에 반영되도록 즉시 반영
		commentRepository.flush();
		return comment;
	}

	@Transactional
	public void delete(Long id, User loginUser) {
		commentRepository.delete(getOwnedComment(id, loginUser));
	}

	/** 게시글 삭제 시 댓글을 함께 삭제한다. */
	@Transactional
	public void deleteAllByPost(Post post) {
		commentRepository.deleteByPostId(post.getId());
	}

	private Comment getOwnedComment(Long id, User loginUser) {
		Comment comment = commentRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
		if (!comment.isWrittenBy(loginUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "댓글 작성자만 가능합니다.");
		}
		return comment;
	}

}
