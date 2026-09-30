package com.quackori.oriweb.post;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	// Fetch the author together to avoid an extra query per post (N+1)
	@Override
	@EntityGraph(attributePaths = "author")
	Page<Post> findAll(Pageable pageable);

	@Override
	@EntityGraph(attributePaths = "author")
	Optional<Post> findById(Long id);

}
