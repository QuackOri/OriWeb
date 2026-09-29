package com.quackori.oriweb.post;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	// 작성자를 함께 조회해서 목록에서 게시글마다 추가 쿼리가 나가지 않게 함
	@Override
	@EntityGraph(attributePaths = "author")
	Page<Post> findAll(Pageable pageable);

	@Override
	@EntityGraph(attributePaths = "author")
	Optional<Post> findById(Long id);

}
