package uk.gov.hmcts.reform.finrem.documentgenerator.e2etest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.documentgenerator.DocumentGeneratorApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.documentResponse;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.documentStoreServiceResponse;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.validConsentOrderOverlayRequest;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
public class ConsentOrderOverlayE2ETest {

    private static final String API_URL = "/version/1/consent-order-overlay";

    @Autowired
    private MockMvc webClient;

    @Value("${service.pdf-service.uri}")
    private String pdfServiceUri;

    @Value("${service.evidence-management-client-api.uri}")
    private String emClientAPIUri;

    @Value("${service.evidence-management-client-api.get-uri}")
    private String evidenceManagementGetEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void before() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    public void storeConsentOrderOverlaySuccessfully() throws Exception {
        externalServicesSetUp();

        MvcResult result = webClient.perform(post(API_URL)
            .content(validConsentOrderOverlayRequest().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(request().asyncStarted())
            .andReturn();

        webClient
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(is(documentResponse())));

        mockRestServiceServer.verify();
    }

    private void externalServicesSetUp() throws Exception {
        mockRestServiceServer.expect(once(), requestTo(emClientAPIUri)).andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .body(documentStoreServiceResponse())
                .contentType(MediaType.APPLICATION_JSON));

        mockRestServiceServer.expect(once(),
            requestTo(evidenceManagementGetEndpoint.concat("?fileUrl=http://documents/fabca3eb")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .body(mainDocContent())
                .contentType(MediaType.APPLICATION_JSON));

        mockRestServiceServer.expect(once(), requestTo(pdfServiceUri)).andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .body(coverPageContent())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private byte[] mainDocContent() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("TEST_MAIN_PAGE.pdf").toURI());
        return Files.readAllBytes(path);
    }

    private byte[] coverPageContent() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("TEST_COVER_PAGE.pdf").toURI());
        return Files.readAllBytes(path);
    }
}
