package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;

import java.util.Map;

@Service
@Slf4j
public class DefaultDocumentManagementService implements DocumentManagementService {
    @Override
    public Document generateAndStoreDocument(String templateName,
                                             Map<String, Object> placeholders,
                                             String authorizationToken) {
        return null;
    }

    @Override
    public Document storeDocument(byte[] document, String authorizationToken) {
        return null;
    }

    @Override
    public byte[] generateDocument(String templateName, Map<String, Object> placeholders) {
        return new byte[0];
    }
}
