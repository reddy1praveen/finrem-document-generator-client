package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import uk.gov.hmcts.reform.finrem.documentgenerator.error.PDFGenerationException;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.Document;
import uk.gov.hmcts.reform.finrem.documentgenerator.model.FileUploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.String.format;

public class DocumentSupport {

    public static Document convert(FileUploadResponse response) {
        return Document.builder()
            .fileName(response.getFileName())
            .url(response.getFileUrl())
            .binaryUrl(toBinaryUrl(response))
            .mimeType(response.getMimeType())
            .createdOn(response.getCreatedOn())
            .build();
    }

    public static byte[] addCoverPage(byte[] coverPageData, byte[] mainDocData) {
        try {
            PDDocument doc = PDDocument.load(mainDocData);

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

    private static String toBinaryUrl(FileUploadResponse response) {
        return format("%s/binary", response.getFileUrl());
    }


}
