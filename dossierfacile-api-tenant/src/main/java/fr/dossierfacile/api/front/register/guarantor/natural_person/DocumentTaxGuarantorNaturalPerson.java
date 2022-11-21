package fr.dossierfacile.api.front.register.guarantor.natural_person;

import fr.dossierfacile.api.front.amqp.Producer;
import fr.dossierfacile.api.front.exception.GuarantorNotFoundException;
import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.SaveStep;
import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentTaxGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.repository.DocumentRepository;
import fr.dossierfacile.api.front.repository.GuarantorRepository;
import fr.dossierfacile.api.front.service.interfaces.ApartmentSharingService;
import fr.dossierfacile.api.front.service.interfaces.DocumentService;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.DocumentPdfGenerationLog;
import fr.dossierfacile.common.entity.Guarantor;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.dossierfacile.common.enums.TypeGuarantor;
import fr.dossierfacile.common.repository.DocumentPdfGenerationLogRepository;
import fr.dossierfacile.common.repository.TenantCommonRepository;
import fr.dossierfacile.common.service.interfaces.DocumentHelperService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fr.dossierfacile.common.enums.DocumentSubCategory.MY_NAME;
import static fr.dossierfacile.common.enums.DocumentSubCategory.OTHER_TAX;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentTaxGuarantorNaturalPerson implements SaveStep<DocumentTaxGuarantorNaturalPersonForm> {

    private final DocumentHelperService documentHelperService;
    private final TenantCommonRepository tenantRepository;
    private final DocumentRepository documentRepository;
    private final TenantMapper tenantMapper;
    private final GuarantorRepository guarantorRepository;
    private final DocumentService documentService;
    private final TenantService tenantService;
    private final Producer producer;
    private final ApartmentSharingService apartmentSharingService;
    private final DocumentPdfGenerationLogRepository documentPdfGenerationLogRepository;

    @Override
    public TenantModel saveStep(Tenant tenant, DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm) {
        Document document = saveDocument(tenant, documentTaxGuarantorNaturalPersonForm);
        producer.generatePdf(document.getId(),
                documentPdfGenerationLogRepository.save(DocumentPdfGenerationLog.builder()
                        .documentId(document.getId())
                        .build()).getId());
        if (Boolean.TRUE.equals(tenant.getHonorDeclaration())) {
            producer.processFileTax(tenant.getId());
        }
        return tenantMapper.toTenantModel(document.getGuarantor().getTenant());
    }

    @Transactional
    Document saveDocument(Tenant tenant, DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm) {
        Guarantor guarantor = guarantorRepository.findByTenantAndTypeGuarantorAndId(tenant, TypeGuarantor.NATURAL_PERSON, documentTaxGuarantorNaturalPersonForm.getGuarantorId())
                .orElseThrow(() -> new GuarantorNotFoundException(documentTaxGuarantorNaturalPersonForm.getGuarantorId()));

        DocumentSubCategory documentSubCategory = documentTaxGuarantorNaturalPersonForm.getTypeDocumentTax();
        Document document = documentRepository.findFirstByDocumentCategoryAndGuarantor(DocumentCategory.TAX, guarantor)
                .orElse(Document.builder()
                        .documentCategory(DocumentCategory.TAX)
                        .guarantor(guarantor)
                        .build());
        document.setDocumentStatus(DocumentStatus.TO_PROCESS);
        document.setDocumentDeniedReasons(null);
        document.setDocumentSubCategory(documentSubCategory);
        document.setCustomText(null);
        if (document.getNoDocument() != null && !document.getNoDocument() && documentTaxGuarantorNaturalPersonForm.getNoDocument()) {
            deleteFilesIfExistedBefore(document);
        }
        document.setNoDocument(documentTaxGuarantorNaturalPersonForm.getNoDocument());
        documentRepository.save(document);

        if (documentSubCategory == MY_NAME
                || (documentSubCategory == OTHER_TAX && !documentTaxGuarantorNaturalPersonForm.getNoDocument())) {
            if (documentTaxGuarantorNaturalPersonForm.getDocuments().size() > 0) {
                documentTaxGuarantorNaturalPersonForm.getDocuments().stream()
                        .filter(f -> !f.isEmpty())
                        .forEach(multipartFile -> documentHelperService.addFile(multipartFile, document));
            } else {
                log.info("Refreshing info in [TAX] document with ID [" + document.getId() + "]");
            }
        }
        if (documentSubCategory == OTHER_TAX && documentTaxGuarantorNaturalPersonForm.getNoDocument()) {
            document.setCustomText(documentTaxGuarantorNaturalPersonForm.getCustomText());
        }
        documentRepository.save(document);
        documentService.initializeFieldsToProcessPdfGeneration(document);
        tenant.lastUpdateDateProfile(LocalDateTime.now(), DocumentCategory.TAX);
        if (tenant.getStatus() == TenantFileStatus.VALIDATED) {
            documentService.resetValidatedDocumentsStatusOfSpecifiedCategoriesToToProcess(guarantor.getDocuments(), List.of(DocumentCategory.PROFESSIONAL, DocumentCategory.FINANCIAL, DocumentCategory.TAX));
        }
        tenantService.updateTenantStatus(tenant);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantRepository.save(tenant);
        return document;
    }

    private void deleteFilesIfExistedBefore(Document document) {
        documentHelperService.deleteFiles(document);
    }
}
