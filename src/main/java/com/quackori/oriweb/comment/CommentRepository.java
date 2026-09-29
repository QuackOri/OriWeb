package com.quackori.oriweb.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@EntityGraph(attributePaths = "author")
	List<Comment> findByPostIdOrderByIdAsc(Long postId);

	@Override
	@EntityGraph(attributePaths = "author")
	Optional<Comment> findById(Long id);

	// 게시글 삭제 시 댓글을 한 번의 쿼리로 삭제
	@Modifying
	@Query("delete from Comment c where c.post.id = :postId")
	void deleteByPostId(@Param("postId") Long postId);

}
