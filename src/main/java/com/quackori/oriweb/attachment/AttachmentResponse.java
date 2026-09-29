package com.quackori.oriweb.attachment;

public record AttachmentResponse(Long id, String originalName, long size, String contentType) {

	public static AttachmentResponse from(Attachment attachment) {
		return new AttachmentResponse(
				attachment.getId(),
				attachment.getOriginalName(),
				attachment.getSize(),
				attachment.getContentType());
	}

}
