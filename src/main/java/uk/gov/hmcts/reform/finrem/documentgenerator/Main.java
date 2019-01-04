package uk.gov.hmcts.reform.finrem.documentgenerator;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Main {

    public static void main(String[] args) throws Exception{
        byte[] docData =  FileUtils.readFileToByteArray(new File("/Users/shaibazkhan/Downloads/006.pdf"));
        PDDocument doc = PDDocument.load(docData);

        byte[] coverData =  FileUtils.readFileToByteArray(new File("/Users/shaibazkhan/Downloads/Coversheet_1.pdf"));
        PDDocument coverDoc = PDDocument.load(coverData);
        PDPage coverPage = coverDoc.getPage(0);

        doc.importPage(coverPage);
        COSDictionary pages = (COSDictionary) doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PAGES);
        COSArray kids = (COSArray) pages.getDictionaryObject(COSName.KIDS);

        COSBase last = kids.get(kids.size() - 1);
        kids.remove(last);
        kids.add(0, last);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        doc.save(bos);
        coverDoc.close();

        try(OutputStream outputStream = new FileOutputStream("/Users/shaibazkhan/Downloads/PDFBoxMerged.pdf")) {
            bos.writeTo(outputStream);
        }

    }

}
