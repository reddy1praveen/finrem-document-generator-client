package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Slf4j
public class DocumentOverlayService {

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private EvidenceManagementService evidenceManagementService;

    @Async
    public CompletableFuture<Document> generateConsentOrderOverlay(String template, Map<String, Object> values,
                                                String authorizationToken) {
        CompletableFuture<byte[]> coverPage = supplyAsync(() -> coverPageData(template, values));
        CompletableFuture<byte[]> consentOrder = supplyAsync(() -> consentOrderData(values, authorizationToken));

        return coverPage
            .thenCombine(consentOrder, DocumentSupport::addCoverPage)
            .thenApply(overlayDoc -> storeDocument(overlayDoc, authorizationToken))
            .thenApply(DocumentSupport::convert);
    }

    private FileUploadResponse storeDocument(byte[] document, String authorizationToken) {
        log.debug("Store document requested with document of size [{}]", document.length);
        FileUploadResponse response = evidenceManagementService.storeDocument(document, authorizationToken);

        return response;
    }

    private byte[] consentOrderData(Map<String, Object> values, String authorizationToken) {
        return evidenceManagementService.retrieveDocument((String) values.get("consentOrderDocUrl"), authorizationToken);
    }

    private byte[] coverPageData(String template, Map<String, Object> values) {
        return pdfGenerationService.generateDocFrom(template, (Map<String, Object>) values.get("coverPageData"));
    }
}
