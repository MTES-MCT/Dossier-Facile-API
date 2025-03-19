package fr.dossierfacile.api.front.register.guarantor.natural_person;

import fr.dossierfacile.api.front.exception.GuarantorNotFoundException;
import fr.dossierfacile.api.front.register.AbstractDocumentSaveStep;
import fr.dossierfacile.api.front.register.SaveStep;
import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentResidencyGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.repository.DocumentRepository;
import fr.dossierfacile.api.front.repository.GuarantorRepository;
import fr.dossierfacile.api.front.service.interfaces.ApartmentSharingService;
import fr.dossierfacile.api.front.service.interfaces.DocumentService;
import fr.dossierfacile.api.front.service.interfaces.TenantStatusService;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.Guarantor;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import fr.dossierfacile.common.repository.TenantCommonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class DocumentResidencyGuarantorNaturalPerson
        extends AbstractDocumentSaveStep<DocumentResidencyGuarantorNaturalPersonForm>
        implements SaveStep<DocumentResidencyGuarantorNaturalPersonForm> {

    private final TenantCommonRepository tenantRepository;
    private final DocumentRepository documentRepository;
    private final GuarantorRepository guarantorRepository;
    private final TenantStatusService tenantStatusService;
    private final ApartmentSharingService apartmentSharingService;

    @Override
    protected Document saveDocument(Tenant tenant, DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm) {
        Guarantor guarantor = guarantorRepository.findByTenantAndTypeGuarantorAndId(tenant, TypeGuarantor.NATURAL_PERSON, documentResidencyGuarantorNaturalPersonForm.getGuarantorId())
                .orElseThrow(() -> new GuarantorNotFoundException(documentResidencyGuarantorNaturalPersonForm.getGuarantorId()));

        DocumentSubCategory documentSubCategory = documentResidencyGuarantorNaturalPersonForm.getTypeDocumentResidency();
        Document document = documentRepository.findFirstByDocumentCategoryAndGuarantor(DocumentCategory.RESIDENCY, guarantor)
                .orElse(Document.builder()
                        .documentCategory(DocumentCategory.RESIDENCY)
                        .guarantor(guarantor)
                        .build());
        document.setDocumentStatus(DocumentStatus.TO_PROCESS);
        document.setDocumentDeniedReasons(null);
        document.setDocumentSubCategory(documentSubCategory);
        document.setDocumentCategoryStep(documentResidencyGuarantorNaturalPersonForm.getCategoryStep());

        if (documentSubCategory == DocumentSubCategory.OTHER_RESIDENCY) {
            document.setCustomText(documentResidencyGuarantorNaturalPersonForm.getCustomText());
            document.setNoDocument(true);
        } else {
            document.setCustomText(null);
            document.setNoDocument(false);
        }
        documentRepository.save(document);

        saveFiles(documentResidencyGuarantorNaturalPersonForm, document);

        tenant.lastUpdateDateProfile(LocalDateTime.now(), DocumentCategory.RESIDENCY);
        tenantStatusService.updateTenantStatus(tenant);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantRepository.save(tenant);
        return document;
    }
}
