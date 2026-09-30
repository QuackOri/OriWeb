package com.quackori.oriweb.attachment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	List<Attachment> findByPostIdOrderByIdAsc(Long postId);

	// The post author is needed to check delete permission
	@Override
	@EntityGraph(attributePaths = {"post", "post.author"})
	Optional<Attachment> findById(Long id);

}
