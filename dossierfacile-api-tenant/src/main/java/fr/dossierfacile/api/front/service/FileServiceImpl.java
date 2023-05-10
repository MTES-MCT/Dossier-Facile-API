package fr.dossierfacile.api.front.service;

import fr.dossierfacile.api.front.exception.FileNotFoundException;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.service.interfaces.DocumentService;
import fr.dossierfacile.api.front.service.interfaces.FileService;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.service.interfaces.LogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final DocumentService documentService;
    private final LogService logService;

    @Override
    @Transactional
    public Document delete(Long id, Tenant tenant) {
        File file = fileRepository.findByIdForAppartmentSharing(id, tenant.getApartmentSharing().getId()).orElseThrow(() -> new FileNotFoundException(id, tenant));

        Document document = file.getDocument();
        fileRepository.delete(file);
        document.getFiles().remove(file);

        logService.saveLog(LogType.ACCOUNT_EDITED, tenant.getId());

        if (document.getFiles().isEmpty()) {
            documentService.delete(document.getId(), document.getTenant() != null ? document.getTenant() : document.getGuarantor().getTenant());
            return null;
        } else {
            documentService.changeDocumentStatus(document, DocumentStatus.TO_PROCESS);
            return document;
        }
    }
}
