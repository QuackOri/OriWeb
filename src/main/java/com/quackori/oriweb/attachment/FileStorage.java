package com.quackori.oriweb.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드 파일을 디스크(app.upload-dir)에 저장/조회/삭제한다.
 * 저장 파일명은 UUID로 새로 만들고, 원래 파일명은 DB(Attachment.originalName)에 보관한다.
 */
@Component
public class FileStorage {

	private final Path uploadDir;

	public FileStorage(@Value("${app.upload-dir}") String uploadDir) {
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.uploadDir);
		} catch (IOException e) {
			throw new UncheckedIOException("업로드 폴더를 만들 수 없습니다: " + this.uploadDir, e);
		}
	}

	/** 파일을 저장하고 저장 파일명을 반환한다. */
	public String save(MultipartFile file) {
		String storedName = UUID.randomUUID().toString();
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, uploadDir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
		}
		return storedName;
	}

	public Resource load(String storedName) {
		return new PathResource(uploadDir.resolve(storedName));
	}

	public void delete(String storedName) {
		try {
			Files.deleteIfExists(uploadDir.resolve(storedName));
		} catch (IOException e) {
			throw new UncheckedIOException("파일 삭제에 실패했습니다.", e);
		}
	}

}
