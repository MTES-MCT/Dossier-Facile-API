package fr.dossierfacile.process.file.util;

import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.dossierfacile.process.file.TestFilesUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryPdfFileTest {

    private static File fileWithPath(String path) {
        return File.builder().storageFile(StorageFile.builder().path(path).build()).build();
    }

    @Test
    void file_with_qr_code() throws IOException {
        File file = fileWithPath("monfranceconnect/student-document.pdf");

        InMemoryPdfFile inMemoryPdfFile = InMemoryPdfFile.create(file, classpathStorageService());

        assertThat(inMemoryPdfFile.hasQrCode()).isTrue();
        assertThat(inMemoryPdfFile.getContentAsString()).isNotEmpty();
    }

    @Test
    void file_with_only_text() throws IOException {
        File file = fileWithPath("test-document.pdf");

        InMemoryPdfFile inMemoryPdfFile = InMemoryPdfFile.create(file, classpathStorageService());

        assertThat(inMemoryPdfFile.hasQrCode()).isFalse();
        assertThat(inMemoryPdfFile.getContentAsString()).isEqualTo("""


                Test document


                """);
    }

    private FileStorageService classpathStorageService() throws IOException {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        when(fileStorageService.download(any(StorageFile.class))).thenAnswer(invocation -> {
            StorageFile file = invocation.getArgument(0, StorageFile.class);
            return TestFilesUtil.getFileAsStream(file.getPath());
        });
        return fileStorageService;
    }

}