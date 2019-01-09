package uk.gov.hmcts.reform.finrem.documentgenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.DocumentRequest;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.io.File;

import static java.lang.String.format;

public class TestResource {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String URL = "some_url";
    public static final String BINARY_URL = format("%s/binary", URL);
    public static final String FILE_NAME = "file_name";
    public static final String CREATED_ON = "11 October 2018";
    public static final String MIME_TYPE = "application/pdf";
    public static final String CREATED_BY = "anonymous";

    public static FileUploadResponse fileUploadResponse() {
        FileUploadResponse response = new FileUploadResponse(HttpStatus.OK);
        response.setFileUrl(URL);
        response.setFileName(FILE_NAME);
        response.setMimeType(MIME_TYPE);
        response.setCreatedOn(CREATED_ON);
        response.setLastModifiedBy(CREATED_BY);
        response.setModifiedOn(CREATED_ON);
        response.setCreatedBy(CREATED_BY);
        return response;
    }

    public static Document document() {
        return Document.builder()
            .url(URL)
            .fileName(FILE_NAME)
            .binaryUrl(BINARY_URL)
            .createdOn(CREATED_ON)
            .mimeType(MIME_TYPE)
            .build();
    }

    public static JsonNode validRequest() throws Exception {
        return MAPPER.readTree(new File(TestResource.class
            .getResource("/fixtures/valid-doc-request.json").toURI()));
    }

    public static JsonNode validConsentOrderOverlayRequest() throws Exception {
        return MAPPER.readTree(new File(TestResource.class
            .getResource("/fixtures/consent-order-overlay-request.json").toURI()));
    }

    public static String templateValuesNotSuppliedRequest() throws JsonProcessingException {
        return MAPPER.writeValueAsString(new DocumentRequest("test", null));
    }

    public static String templateNotSuppliedRequest() throws JsonProcessingException {
        return MAPPER.writeValueAsString(new DocumentRequest(null, ImmutableMap.of()));
    }

    public static byte[] pdfServiceResponse() throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(new byte[]{1});
    }

    public static String documentStoreServiceResponse() throws JsonProcessingException {
        final FileUploadResponse fileUploadResponse = fileUploadResponse();

        return MAPPER.writeValueAsString(ImmutableList.of(fileUploadResponse));
    }

    public static String documentResponse() throws JsonProcessingException {
        return MAPPER.writeValueAsString(document());
    }
}
