package fr.dossierfacile.process.file.service;

import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.enums.FileStorageStatus;
import fr.dossierfacile.common.repository.StorageFileRepository;
import fr.dossierfacile.common.service.interfaces.DocumentHelperService;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.dossierfacile.process.file.repository.FileRepository;
import fr.dossierfacile.process.file.service.interfaces.MinifyFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinifyFileServiceImpl implements MinifyFileService {

    private final FileRepository fileRepository;
    private final StorageFileRepository storageFileRepository;
    private final FileStorageService fileStorageService;
    private final DocumentHelperService documentHelperService;

    @Override
    public void process(Long fileId) {
        fileRepository.findById(fileId.longValue())
                .ifPresent(file -> {
                    try (InputStream inputStream = fileStorageService.download(file.getStorageFile())) {
                        StorageFile storageFile = documentHelperService.generatePreview(inputStream, file.getStorageFile().getName());

                        // This is a long operation and method is not transactional - refresh to check status
                        Optional<File> dbFile = fileRepository.findById(file.getId());
                        if (dbFile.isPresent()) {
                            dbFile.get().setPreview(storageFile);
                            fileRepository.save(dbFile.get());
                        } else {
                            storageFile.setStatus(FileStorageStatus.TO_DELETE);
                            storageFileRepository.save(storageFile);
                        }

                    } catch (Exception e) {
                        log.error(e.getMessage(), e.getCause());
                    }
                });
    }

}
