package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import static java.lang.String.format;

public class DocumentSupport {

    public static Document convert(FileUploadResponse response) {
        return Document.builder()
            .fileName(response.getFileName())
            .url(response.getFileUrl())
            .binaryUrl(toBinaryUrl(response))
            .mimeType(response.getMimeType())
            .createdOn(response.getCreatedOn())
            .build();
    }

    private static String toBinaryUrl(FileUploadResponse response) {
        return format("%s/binary", response.getFileUrl());
    }
}
