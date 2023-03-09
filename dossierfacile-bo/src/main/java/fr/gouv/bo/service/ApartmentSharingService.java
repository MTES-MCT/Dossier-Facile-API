package fr.gouv.bo.service;

import fr.dossierfacile.common.entity.ApartmentSharing;
import fr.dossierfacile.common.enums.FileStatus;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.gouv.bo.dto.ApartmentSharingDTO01;
import fr.gouv.bo.repository.BOApartmentSharingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class ApartmentSharingService {

    private final BOApartmentSharingRepository apartmentSharingRepository;
    private final FileStorageService fileStorageService;

    public ApartmentSharing find(Long id) {
        return apartmentSharingRepository.getOne(id);
    }

    public void delete(ApartmentSharing apartmentSharing) {
        apartmentSharingRepository.delete(apartmentSharing);
    }

    public void save(ApartmentSharing apartmentSharing) {
        apartmentSharingRepository.save(apartmentSharing);
    }

    public Page<ApartmentSharingDTO01> findAll(Pageable pageable) {
        return apartmentSharingRepository.findAllByOrderByIdDesc(pageable);
    }

    public ApartmentSharing findOne(Long id) {
        return apartmentSharingRepository.getOne(id);
    }

    @Transactional
    public void resetDossierPdfGenerated(ApartmentSharing apartmentSharing) {
        String currentUrl = apartmentSharing.getUrlDossierPdfDocument();
        if (currentUrl != null) {
            fileStorageService.delete(currentUrl);
            apartmentSharing.setUrlDossierPdfDocument(null);
            apartmentSharing.setDossierPdfDocumentStatus(FileStatus.DELETED);
            apartmentSharingRepository.save(apartmentSharing);
        }
    }

    public void refreshUpdateDate(ApartmentSharing apartmentSharing) {
        apartmentSharing.setLastUpdateDate(new Date());
        apartmentSharingRepository.save(apartmentSharing);
    }
}
