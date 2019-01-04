package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.PDFGenerationException;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        CompletableFuture<byte[]> coverPage = CompletableFuture.supplyAsync(() -> coverPageData(template, values));
        CompletableFuture<byte[]> consentOrder = CompletableFuture.supplyAsync(() -> consentOrderData(values, authorizationToken));

        return coverPage
            .thenCombine(consentOrder, this::merge)
            .thenApply(overlayDoc -> storeDocument(overlayDoc, authorizationToken))
            .thenApply(DocumentSupport::convert);
    }

    private byte[] merge(byte[] coverPageData, byte[] consentOrderData) {
        try {
            PDDocument doc = PDDocument.load(consentOrderData);

            PDDocument coverDoc = PDDocument.load(coverPageData);
            PDPage coverPage = coverDoc.getPage(0);
            doc.importPage(coverPage);


            doc.importPage(coverPage);
            COSDictionary pages = (COSDictionary) doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PAGES);
            COSArray kids = (COSArray) pages.getDictionaryObject(COSName.KIDS);

            COSBase last = kids.get(kids.size() - 1);
            kids.remove(last);
            kids.add(0, last);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            doc.save(bos);
            coverDoc.close();

            return bos.toByteArray();
        } catch (IOException e) {
            throw new PDFGenerationException("couldn't merge documents", e);
        }
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
