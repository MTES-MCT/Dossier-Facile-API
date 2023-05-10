package fr.dossierfacile.api.pdfgenerator.service.templates;

import fr.dossierfacile.api.pdfgenerator.model.FileInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.any;

// Used for manual testing
@Disabled
@ExtendWith(MockitoExtension.class)
public class IdentificationBOPdfDocumentTemplateTest {

    @Mock
    MessageSource messageSource;
    @InjectMocks
    BOIdentificationPdfDocumentTemplate boPdfDocumentTemplate;

    @BeforeEach
    void init() {
        Mockito.lenient().when(messageSource.getMessage(any(),any(),any(),any() )).thenReturn(BOPdfDocumentTemplate.DEFAULT_WATERMARK);
    }


    @Disabled
    @Test
    public void check_render_with_rotated_files() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI_rotated.jpeg");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaType.IMAGE_JPEG)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultReverse.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Arrays.asList(data)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @Disabled
    @DisplayName("Check if the pdf file is correctly generated in specific files")
    @Test
    public void check_render_with_special_files() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("secret/TestID1.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaType.APPLICATION_PDF)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultSpecial.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Arrays.asList(data)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated and with enough quality from landscape ID pdf")
    @Test
    public void check_render_with_landscape_pdf() throws IOException {
        InputStream is = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("landscapeid.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaType.APPLICATION_PDF)
                .inputStream(is)
                .build();
        InputStream is2 = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("landscapeid2.pdf");
        FileInputStream data2 = FileInputStream
                .builder()
                .mediaType(MediaType.APPLICATION_PDF)
                .inputStream(is2)
                .build();

        File resultFile = new File("target/resultLandscape.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Arrays.asList(data, data2)));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from all type textual pdf, pdf, image")
    @Test
    public void check_render_with_all_type() throws IOException {
        InputStream is = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");
        InputStream isJPG = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        InputStream isHJPG = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNIHorizontale.jpg");
        InputStream isTextPdf = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("landscapeid.pdf");
        InputStream isOpen = IdentificationBOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        List<FileInputStream> dataList =
                Arrays.asList(FileInputStream
                                .builder()
                                .mediaType(MediaType.APPLICATION_PDF)
                                .inputStream(is)
                                .build(),
                        FileInputStream
                                .builder()
                                .mediaType(MediaType.IMAGE_JPEG)
                                .inputStream(isJPG)
                                .build(),
                        FileInputStream
                                .builder()
                                .mediaType(MediaType.IMAGE_JPEG)
                                .inputStream(isHJPG)
                                .build(),

                        FileInputStream
                                .builder()
                                .mediaType(MediaType.APPLICATION_PDF)
                                .inputStream(isTextPdf)
                                .build(),
                        FileInputStream
                                .builder()
                                .mediaType(MediaType.APPLICATION_PDF)
                                .inputStream(isOpen)
                                .build());

        File resultFile = new File("target/resultFullTypeTestPdf.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(dataList));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }
}
