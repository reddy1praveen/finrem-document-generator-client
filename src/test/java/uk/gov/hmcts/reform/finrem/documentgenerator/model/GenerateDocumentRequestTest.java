package uk.gov.hmcts.reform.finrem.documentgenerator.model;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenerateDocumentRequestTest {

    public static final String TEMPLATE = "template";

    @Test
    public void properties() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(TEMPLATE, ImmutableMap.of());
        assertEquals(request.getTemplate(), TEMPLATE);
        assertEquals(request.getValues(), ImmutableMap.of());
    }
}
