package uk.gov.hmcts.reform.finrem.documentgenerator.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        itext();
        pdfBoxDocument();
    }

    private static void itext() throws IOException, DocumentException {
        PdfReader readerOriginalDoc = new PdfReader("/Users/shaibazkhan/Downloads/response.pdf");
        PdfStamper stamper = new PdfStamper(
            readerOriginalDoc, new FileOutputStream("/Users/shaibazkhan/Downloads/NewStamper.pdf"));

        PdfContentByte content = stamper.getUnderContent(1);
        Image image = Image.getInstance("/Users/shaibazkhan/Downloads/signature.jpg");
        image.scaleAbsolute(200, 200);
        image.setAbsolutePosition(100, 100);
        content.addImage(image, false);

        PdfContentByte canvas = stamper.getOverContent(1);
        ColumnText.showTextAligned(canvas,
            Element.ALIGN_RIGHT, new Phrase("Signed by"), 400, 200, 0);

        stamper.close();
    }

    public static void pdfBoxDocument() throws Exception {
        File file = new File("/Users/shaibazkhan/Downloads/response.pdf");
        PDDocument doc = PDDocument.load(file);
        //Retrieving the page
        PDPage page = doc.getPage(0);

        Overlay overlayObj = new Overlay();
        PDFont font = PDType1Font.COURIER_OBLIQUE;


        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
            true, false);
        contentStream.setFont(font, 50);
        contentStream.setNonStrokingColor(0);
        contentStream.beginText();
        contentStream.newLineAtOffset(200, 200);
        contentStream.showText("Signed by");  // deprecated. Use showText(String text)
        contentStream.endText();
        contentStream.close();

        doc.save("/Users/shaibazkhan/Downloads/PdfBoxStamper.pdf");
//        doc.close();

        PDImageXObject pdImage = PDImageXObject.createFromFile("/Users/shaibazkhan/Downloads/signature.jpg", doc);
        contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
            true, false);
        contentStream.drawImage(pdImage, 70, 250, 200, 200);
        contentStream.close();

        doc.save("/Users/shaibazkhan/Downloads/PdfBoxStamper.pdf");
        doc.close();


//        overlayObj.setOverlayPosition(Overlay.Position.BACKGROUND);
//        overlayObj.setInputPDF(doc);
//
//        overlayObj.setAllPagesOverlayPDF(overlayDoc);
    }
}
