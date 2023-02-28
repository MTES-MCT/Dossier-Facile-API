package fr.dossierfacile.common.service;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.EncryptionKey;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.ObjectStorageProvider;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.repository.SharedFileRepository;
import fr.dossierfacile.common.service.interfaces.DocumentHelperService;
import fr.dossierfacile.common.service.interfaces.EncryptionKeyService;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.dossierfacile.common.utils.FileUtility;
import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentHelperServiceImpl implements DocumentHelperService {
    private final FileStorageService fileStorageService;
    private final SharedFileRepository fileRepository;
    private final EncryptionKeyService encryptionKeyService;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public File addFile(MultipartFile multipartFile, Document document) {
        EncryptionKey encryptionKey = encryptionKeyService.getCurrentKey();
        String path = fileStorageService.uploadFile(multipartFile, encryptionKey);


        File file = File.builder()
                .path(path)
                .document(document)
                .originalName(multipartFile.getOriginalFilename())
                .size(multipartFile.getSize())
                .contentType(multipartFile.getContentType())
                .key(encryptionKey)
                .numberOfPages(FileUtility.countNumberOfPagesOfPdfDocument(multipartFile))
                .build();
        file = fileRepository.save(file);
        if (!document.getFiles().contains(file)){
            document.getFiles().add(file);
        }
        return file;
    }

    @Override
    public void deleteFiles(Document document) {
        if (document.getFiles() != null && !document.getFiles().isEmpty()) {
            document.setFiles(null);
            fileRepository.deleteAll(document.getFiles());
            fileStorageService.delete(document.getFiles().stream().map(File::getPath).collect(Collectors.toList()));
        }
    }

    @Override
    public StorageFile generatePreview(InputStream fileInputStream, String originalName) {
        try {
            String imageExtension = FilenameUtils.getExtension(originalName);
            BufferedImage preview;
            if ("pdf".equalsIgnoreCase(imageExtension)) {
                long startTime = System.currentTimeMillis();
                try (PDDocument document = PDDocument.load(fileInputStream)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 200, ImageType.RGB);
                    preview = resizeImage(bufferedImage);
                    log.info("resize pdf duration : " + (System.currentTimeMillis() - startTime));
                }
            } else {
                preview = resizeImage(ImageIO.read(fileInputStream));
            }
            if (preview == null) {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(preview, "jpg", baos);

            StorageFile storageFile = StorageFile.builder()
                    .name(originalName)
                    .contentType(MediaType.IMAGE_JPEG_VALUE)
                    .provider(ObjectStorageProvider.OVH)
                    .encryptionKey(encryptionKeyService.getCurrentKey())
                    .build();

            // TODO - Next step use Factory for having multiple storage services
            // storageFile StorageFactory.getStorageService().upload(compressImage, storageFile);
            try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
                return fileStorageService.upload(is, storageFile);
            }
        } catch (Exception e) {
            log.error(e.getMessage() + " " + Sentry.captureException(e), e);
        }
        return null;
    }

    BufferedImage resizeImage(BufferedImage image) throws IOException {
        if (image == null) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        float originalWidth = image.getWidth();
        float originalHeight = image.getHeight();
        int targetWidth = 300;
        int targetHeight = targetWidth < originalWidth ? (int) (targetWidth / originalWidth * originalHeight) : 300;
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        log.info("resize image duration : " + duration);
        return resizedImage;
    }

}
