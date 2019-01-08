package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.fileUploadResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@TestPropertySource(locations = "/application.properties")
public class EvidenceManagementServiceTest {

    private static final String SAVE_DOC_URL = "http://localhost:4006/emclientapi/version/1/upload";
    private static final String GET_DOC_URL = "http://localhost:4006/emclientapi/version/1/files";
    private static final String AUTH_TOKEN = "Bearer KJBUYVBJLIJBIBJHBbhjbiyYVIUJHV";
    public static final String DOC_CONTENT = "welcome doc";
    private static final String DOC_NAME = "document_name";
    public static final String FILE_URL = "http://dm-store/JKlkm";

    @Autowired
    private EvidenceManagementService service;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void storeDocument() throws JsonProcessingException {
        mockServer.expect(requestTo(SAVE_DOC_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Content-Type", containsString(MediaType.MULTIPART_FORM_DATA_VALUE)))
            .andExpect(header("Authorization", equalTo(AUTH_TOKEN)))
            .andRespond(withSuccess(jsonResponse(fileUploadResponse()), MediaType.APPLICATION_JSON));

        FileUploadResponse result = service.storeDocument(DOC_NAME, DOC_CONTENT.getBytes(), AUTH_TOKEN);
        assertThat(result, is(equalTo(fileUploadResponse())));

        mockServer.verify();
    }

    @Test(expected = DocumentStorageException.class)
    public void storeDocumentDocumentStorageError() throws JsonProcessingException {
        mockServer.expect(requestTo(SAVE_DOC_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Content-Type", containsString(MediaType.MULTIPART_FORM_DATA_VALUE)))
            .andExpect(header("Authorization", equalTo(AUTH_TOKEN)))
            .andRespond(
                withSuccess(jsonResponse(new FileUploadResponse(HttpStatus.BAD_REQUEST)), MediaType.APPLICATION_JSON));

        service.storeDocument(DOC_NAME, DOC_CONTENT.getBytes(), AUTH_TOKEN);

        mockServer.verify();
    }

    @Test
    public void retrieveDocumentSuccess() {
        mockServer.expect(requestTo(GET_DOC_URL.concat("?fileUrl=").concat(FILE_URL)))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", equalTo(AUTH_TOKEN)))
            .andRespond(withSuccess("hello".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        byte[] bytes = service.retrieveDocument(FILE_URL, AUTH_TOKEN);
        assertThat(bytes, is("hello".getBytes()));
        mockServer.verify();
    }

    @Test(expected = HttpClientErrorException.class)
    public void retrieveDocumentError() {
        mockServer.expect(requestTo(GET_DOC_URL.concat("?fileUrl=").concat(FILE_URL)))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", equalTo(AUTH_TOKEN)))
            .andRespond(withBadRequest());

        service.retrieveDocument(FILE_URL, AUTH_TOKEN);
    }

    private String jsonResponse(FileUploadResponse fileUploadResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(Arrays.asList(fileUploadResponse));
    }
}
