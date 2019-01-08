package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.finrem.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.PDFGenerationException;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.document;
import static uk.gov.hmcts.reform.finrem.documentgenerator.TestResource.fileUploadResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@TestPropertySource(locations = "/application.properties")
public class DocumentOverlayServiceTest {

    private static final String TEMPLATE_NAME = "templateName";
    private static String FILE_URL = "file_url";
    private static final ImmutableMap<String, Object> DATA = ImmutableMap.of("name", "value");
    private static final ImmutableMap<String, Object> PLACEHOLDERS =
        ImmutableMap.of("consentOrderDocUrl", FILE_URL, "coverPageData", DATA);
    private static final String AUTH_TOKEN = "Bearer BBJHJbbIIBHBLB";


    @Autowired
    private DocumentOverlayService service;

    @MockBean
    private PDFGenerationService pdfGenerationService;

    @MockBean
    private EvidenceManagementService evidenceManagementService;

    @Test
    public void storeConsentOrderOverlayWithSuccess() throws Exception {
        setUpDocumentRetrievals(invocationOnMock -> mainDocContent(), invocationOnMock -> coverPageContent());
        when(
            evidenceManagementService.storeDocument(eq("consent-order.pdf"), isA(byte[].class), eq(AUTH_TOKEN)))
            .thenReturn(fileUploadResponse());

        CompletableFuture<Document> future = service.generateConsentOrderOverlay(TEMPLATE_NAME, PLACEHOLDERS, AUTH_TOKEN);
        Document document = future.get();
        assertThat(document, is(document()));
    }

    @Test
    public void storeConsentOrderOverlayPdfGenerationError() throws Exception {
        setUpDocumentRetrievals(
            invocationOnMock ->  { throw new PDFGenerationException("error", new Exception()); },
            invocationOnMock -> coverPageContent());

        try {
            service.generateConsentOrderOverlay(TEMPLATE_NAME, PLACEHOLDERS, AUTH_TOKEN).get();
            fail("should have thrown PDFGenerationException");
        } catch (Exception e) {
            assertThat(e.getCause(), is(instanceOf(PDFGenerationException.class)));
        }
    }

    @Test
    public void storeConsentOrderOverlayDocumentRetrievalError() throws Exception{
        setUpDocumentRetrievals(
            invocationOnMock -> mainDocContent(),
            invocationOnMock -> { throw new HttpClientErrorException(SERVICE_UNAVAILABLE); });

        try {
            service.generateConsentOrderOverlay(TEMPLATE_NAME, PLACEHOLDERS, AUTH_TOKEN).get();
            fail("should have thrown PDFGenerationException");
        } catch (Exception e) {
            assertThat(e.getCause(), is(instanceOf(HttpClientErrorException.class)));
        }
    }

    @Test
    public void storeConsentOrderOverlayDocumentStoreError() throws Exception{
        setUpDocumentRetrievals(
            invocationOnMock -> mainDocContent(),
            invocationOnMock -> coverPageContent());
        when(
            evidenceManagementService.storeDocument(eq("consent-order.pdf"), isA(byte[].class), eq(AUTH_TOKEN)))
            .thenThrow(new DocumentStorageException("error"));

        try {
            service.generateConsentOrderOverlay(TEMPLATE_NAME, PLACEHOLDERS, AUTH_TOKEN).get();
            fail("should have thrown DocumentStorageException");
        } catch (Exception e) {
            assertThat(e.getCause(), is(instanceOf(DocumentStorageException.class)));
        }
    }

    private void setUpDocumentRetrievals(Answer<byte[]> pdf, Answer<byte[]> coverPage) {
        when(pdfGenerationService.generateDocFrom(TEMPLATE_NAME, DATA)).thenAnswer(pdf);
        when(evidenceManagementService.retrieveDocument(FILE_URL, AUTH_TOKEN)).thenAnswer(coverPage);
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
