package uk.gov.hmcts.reform.finrem.documentgenerator.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.DocumentRequest;
import uk.gov.hmcts.reform.finrem.documentgenerator.service.DocumentOverlayService;

import java.util.concurrent.CompletableFuture;

@RestController
@Api(value = "Document Overlay", tags = {"Document Overlay"})
@Slf4j
public class DocumentOverlayController {


    @Autowired
    private DocumentOverlayService documentOverlayService;

    @Async
    @PostMapping("/version/1/consent-order-overlay")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Consent order with cover-page was generated successfully" +
            " and stored in the evidence management."
            + " Returns the url to the stored document.", response = String.class),
        @ApiResponse(code = 400, message = "Returned when input parameters are invalid or template not found",
            response = String.class),
        @ApiResponse(code = 503, message = "Returned when the PDF Service or Evidence Management Client Api "
            + "cannot be reached", response = String.class),
        @ApiResponse(code = 500, message = "Returned when there is an unknown server error",
            response = String.class)
    })
    public CompletableFuture<Document> consentOrderOverlay(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        DocumentRequest templateData) {
        log.info("Consent order overlay requested with templateName [{}], placeholders map of size[{}]",
            templateData.getTemplate(), templateData.getValues().size());

        return documentOverlayService.generateConsentOrderOverlay(
            templateData.getTemplate(), templateData.getValues(), authorizationToken
        );
    }
}
