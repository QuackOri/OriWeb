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
 * Saves, loads and deletes uploaded files on disk (app.upload-dir).
 * Stored file names are new UUIDs; the original name is kept in the DB (Attachment.originalName).
 */
@Component
public class FileStorage {

	private final Path uploadDir;

	public FileStorage(@Value("${app.upload-dir}") String uploadDir) {
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.uploadDir);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot create upload directory: " + this.uploadDir, e);
		}
	}

	/** Saves the file and returns the stored file name. */
	public String save(MultipartFile file) {
		String storedName = UUID.randomUUID().toString();
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, uploadDir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to save file.", e);
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
			throw new UncheckedIOException("Failed to delete file.", e);
		}
	}

}
