package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class EvidenceManagementService {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String FILE_PARAMETER = "file";

    @Value("${service.evidence-management-client-api.uri}")
    private String evidenceManagementEndpoint;

    @Value("${service.evidence-management-client-api.get-uri}")
    private String evidenceManagementGetEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    public FileUploadResponse storeDocument(String documentName, byte[] document, String authorizationToken) {
        log.info("Save document call to evidence management is made document of size [{}]", document.length);

        FileUploadResponse fileUploadResponse = save(documentName, document, authorizationToken);

        if (fileUploadResponse.getStatus() == HttpStatus.OK) {
            return fileUploadResponse;
        } else {
            throw new DocumentStorageException("Failed to store document");
        }
    }

    public byte[] retrieveDocument(String fileUrl, String authorizationToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(evidenceManagementGetEndpoint);
        builder.queryParam("fileUrl", fileUrl);

        return restTemplate.exchange(
            builder.build().encode().toUriString(),
            HttpMethod.GET,
            new HttpEntity<>(getAuthHttpHeaders(authorizationToken)),
            byte[].class).getBody();
    }

    private FileUploadResponse save(String documentName, byte[] document, String authorizationToken) {
        requireNonNull(document);

        ResponseEntity<List<FileUploadResponse>> responseEntity = restTemplate.exchange(evidenceManagementEndpoint,
                HttpMethod.POST,
                new HttpEntity<>(
                        buildStoreDocumentRequest(document, documentName),
                    getHttpHeaders(authorizationToken)),
                new ParameterizedTypeReference<List<FileUploadResponse>>() {
                });

        return responseEntity.getBody().get(0);
    }

    private HttpHeaders getHttpHeaders(String authToken) {
        HttpHeaders headers = getAuthHttpHeaders(authToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private LinkedMultiValueMap<String, Object> buildStoreDocumentRequest(byte[] document, String filename) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        HttpEntity<Resource> httpEntity = new HttpEntity<>(new ByteArrayResource(document) {
            @Override
            public String getFilename() {
                return filename;
            }
        }, headers);

        parameters.add(FILE_PARAMETER, httpEntity);
        return parameters;
    }

    private HttpHeaders getAuthHttpHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, authToken);

        return headers;
    }
}
