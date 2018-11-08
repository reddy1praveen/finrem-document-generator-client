package uk.gov.hmcts.reform.finrem.documentgenerator.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocumentTest {

    public static final String CREATED_ON = "20th November 2018";
    public static final String MIME_TYPE = "application/json";
    public static final String URL = "url";

    @Test
    public void properties() {
        Document doc = doc();

        assertEquals(doc.getCreatedOn(), CREATED_ON);
        assertEquals(doc.getMimeType(), MIME_TYPE);
        assertEquals(doc.getUrl(), URL);
    }

    private Document doc() {
        Document doc = new Document();
        doc.setCreatedOn(CREATED_ON);
        doc.setMimeType(MIME_TYPE);
        doc.setUrl(URL);
        return doc;
    }
}
