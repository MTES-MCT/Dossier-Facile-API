package fr.dossierfacile.api.pdfgenerator.service.templates;

import fr.dossierfacile.api.pdfgenerator.model.FileInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

// Used for manual testing
@Disabled
public class BOPdfDocumentTemplateTest {


    @DisplayName("Check if the render is correctly generated from text pdf and wrong sized pdf")
    @Test
    public void check_render_with_text_pdf() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("bigH.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .extension("pdf")
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultTestPdfWrongSize.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(new BOPdfDocumentTemplate().render(Arrays.asList(data)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from all type textual pdf, pdf, image, non obfuscable pdf")
    @Test
    public void check_render_with_all_type() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");
        InputStream isJPG = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        InputStream isTextPdf = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("page1.pdf");
        InputStream isOpen = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .extension("pdf")
                .inputStream(is)
                .build();
        FileInputStream data2 = FileInputStream
                .builder()
                .extension("jpg")
                .inputStream(isJPG)
                .build();
        FileInputStream data3 = FileInputStream
                .builder()
                .extension("pdf")
                .inputStream(isTextPdf)
                .build();
        FileInputStream data4 = FileInputStream
                .builder()
                .extension("pdf")
                .inputStream(isOpen)
                .build();

        File resultFile = new File("target/resultFullTypeTestPdf.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(new BOPdfDocumentTemplate().render(Arrays.asList(data, data2, data3, data4)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from image pdf")
    @Test
    public void check_render_with_img_pdf() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .extension("pdf")
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultTestPdfWithJpeg.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(new BOPdfDocumentTemplate().render(Arrays.asList(data)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from jpegs")
    @Test
    public void check_render_from_jpegs() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        InputStream is2 = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNIHorizontale.jpg");
        InputStream is3 = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNISmall.jpg");

        FileInputStream data = FileInputStream
                .builder()
                .extension("jpg")
                .inputStream(is)
                .build();
        FileInputStream data2 = FileInputStream
                .builder()
                .extension("jpg")
                .inputStream(is2)
                .build();
        FileInputStream data3 = FileInputStream
                .builder()
                .extension("jpg")
                .inputStream(is3)
                .build();
        File resultFile = new File("target/resultTestJpeg.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(new BOPdfDocumentTemplate().render(Arrays.asList(data, data2, data3)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }
}
