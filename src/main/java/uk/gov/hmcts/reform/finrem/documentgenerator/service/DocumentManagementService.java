package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.documentgenerator.service.DocumentSupport.convert;

@Service
@Slf4j
public class DocumentManagementService {
    private static final String PDF_FILE_NAME = "MiniFormA.pdf";

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private EvidenceManagementService evidenceManagementService;

    public Document storeDocument(String templateName,
                                  Map<String, Object> placeholders,
                                  String authorizationToken) {
        log.debug("Generate and Store Document requested with templateName [{}], placeholders of size [{}]",
            templateName, placeholders.size());

        return storeDocument(
            pdfGenerationService.generateDocFrom(templateName, placeholders),
            authorizationToken);
    }

    private Document storeDocument(byte[] document, String authorizationToken) {
        log.debug("Store document requested with document of size [{}]", document.length);
        FileUploadResponse response =
            evidenceManagementService.storeDocument(PDF_FILE_NAME, document, authorizationToken);

        return convert(response);
    }

}
