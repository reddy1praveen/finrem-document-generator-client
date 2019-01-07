package uk.gov.hmcts.reform.finrem.documentgenerator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.DocumentRequest;
import uk.gov.hmcts.reform.finrem.documentgenerator.service.DocumentOverlayService;

import javax.ws.rs.core.MediaType;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.CREATED_ON;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.MIME_TYPE;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.URL;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.document;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentOverlayController.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
public class DocumentOverlayControllerTest {

    public static final String TEMPLATE = "template";
    public static final Map<String, Object> VALUES = ImmutableMap.of("name", "value");
    public static final String AUTH_TOKEN = "authToken";
    public static final String API_URL = "/version/1/consent-order-overlay";
    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private DocumentOverlayService documentOverlayService;

    private MockMvc mvc;

    private String requestContent;

    @Before
    public void setUp() throws JsonProcessingException {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        requestContent = new ObjectMapper().writeValueAsString(new DocumentRequest(TEMPLATE, VALUES));
    }

    @Test
    public void generateConsentOrderOverlaySuccess() throws Exception {
        when(documentOverlayService.generateConsentOrderOverlay(TEMPLATE, VALUES, AUTH_TOKEN))
            .thenReturn(completedFuture(document()));

        MvcResult mvcResult = mvc.perform(post(API_URL)
            .content(requestContent)
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc
            .perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url", Matchers.is(URL)))
            .andExpect(jsonPath("$.mimeType", Matchers.is(MIME_TYPE)))
            .andExpect(jsonPath("$.createdOn", Matchers.is(CREATED_ON)))
            .andExpect(jsonPath("$.fileName", Matchers.is(FILE_NAME)))
            .andExpect(jsonPath("$.binaryUrl", Matchers.is(BINARY_URL)));
    }

    @Test
    public void generateConsentOrderOverlayBadRequest() throws Exception {
        when(documentOverlayService.generateConsentOrderOverlay(TEMPLATE, VALUES, AUTH_TOKEN))
            .thenReturn(completedFuture(document()));

         mvc.perform(post(API_URL)
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void generateConsentOrderOverlayInternalError() throws Exception {
        when(documentOverlayService.generateConsentOrderOverlay(TEMPLATE, VALUES, AUTH_TOKEN))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        MvcResult mvcResult = mvc.perform(post(API_URL)
            .content(requestContent)
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc
            .perform(asyncDispatch(mvcResult))
            .andExpect(status().isInternalServerError());
    }
}
