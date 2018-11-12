package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;

import java.util.Map;

public interface DocumentManagementService {
    Document generateAndStoreDocument(String templateName, Map<String, Object> placeholders,
                                      String authorizationToken);

    Document storeDocument(byte[] document, String authorizationToken);

    byte[] generateDocument(String templateName, Map<String, Object> placeholders);
}
