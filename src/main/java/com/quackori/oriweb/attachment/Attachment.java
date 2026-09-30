package com.quackori.oriweb.attachment;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quackori.oriweb.post.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	/** Original file name uploaded by the user (used as the download file name) */
	@Column(nullable = false)
	private String originalName;

	/** File name actually stored in the upload directory (UUID) */
	@Column(nullable = false, unique = true)
	private String storedName;

	@Column(nullable = false)
	private long size;

	private String contentType;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Attachment(Post post, String originalName, String storedName, long size, String contentType) {
		this.post = post;
		this.originalName = originalName;
		this.storedName = storedName;
		this.size = size;
		this.contentType = contentType;
	}

}
