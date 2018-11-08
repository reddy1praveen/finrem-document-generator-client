package uk.gov.hmcts.reform.finrem.documentgenerator.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;

public class FileUploadResponseTest {

    public static final String URL = "url";
    public static final String FILE_NAME = "name";
    public static final String MIME_TYPE = "application/text";
    public static final String CREATED_BY = "test";
    public static final String CREATED_ON = "20th Nov 2018";

    @Test
    public void properties() {
        FileUploadResponse response = fileUploadResponse();

        assertEquals(response.getStatus(), OK);
        assertEquals(response.getFileUrl(), URL);
        assertEquals(response.getFileName(), FILE_NAME);
        assertEquals(response.getMimeType(), MIME_TYPE);
        assertEquals(response.getCreatedBy(), CREATED_BY);
        assertEquals(response.getLastModifiedBy(), CREATED_BY);
        assertEquals(response.getCreatedOn(), CREATED_ON);
        assertEquals(response.getModifiedOn(), CREATED_ON);
    }

    private FileUploadResponse fileUploadResponse() {
        FileUploadResponse response = new FileUploadResponse(OK);
        response.setFileUrl(URL);
        response.setFileName(FILE_NAME);
        response.setMimeType(MIME_TYPE);
        response.setCreatedBy(CREATED_BY);
        response.setLastModifiedBy(CREATED_BY);
        response.setCreatedOn(CREATED_ON);
        response.setModifiedOn(CREATED_ON);
        return response;
    }

}
