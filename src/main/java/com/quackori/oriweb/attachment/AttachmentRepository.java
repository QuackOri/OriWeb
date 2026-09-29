package com.quackori.oriweb.attachment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	List<Attachment> findByPostIdOrderByIdAsc(Long postId);

	// 삭제 권한 확인 시 게시글 작성자까지 필요
	@Override
	@EntityGraph(attributePaths = {"post", "post.author"})
	Optional<Attachment> findById(Long id);

}
