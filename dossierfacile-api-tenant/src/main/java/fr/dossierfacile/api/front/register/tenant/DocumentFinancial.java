package fr.dossierfacile.api.front.register.tenant;

import fr.dossierfacile.api.front.register.AbstractDocumentSaveStep;
import fr.dossierfacile.api.front.register.SaveStep;
import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.api.front.repository.DocumentRepository;
import fr.dossierfacile.api.front.service.interfaces.ApartmentSharingService;
import fr.dossierfacile.api.front.service.interfaces.DocumentService;
import fr.dossierfacile.api.front.service.interfaces.TenantStatusService;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.dossierfacile.common.repository.TenantCommonRepository;
import fr.dossierfacile.common.service.interfaces.DocumentHelperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentFinancial extends AbstractDocumentSaveStep<DocumentFinancialForm> implements SaveStep<DocumentFinancialForm> {

    private final DocumentHelperService documentHelperService;
    private final TenantCommonRepository tenantRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final TenantStatusService tenantStatusService;
    private final ApartmentSharingService apartmentSharingService;

    @Override
    protected Document saveDocument(Tenant tenant, DocumentFinancialForm documentFinancialForm) {
        DocumentSubCategory documentSubCategory = documentFinancialForm.getTypeDocumentFinancial();
        Document document = documentRepository.findByDocumentCategoryAndTenantAndId(DocumentCategory.FINANCIAL, tenant, documentFinancialForm.getId())
                .orElse(Document.builder()
                        .documentCategory(DocumentCategory.FINANCIAL)
                        .documentCategoryStep(documentFinancialForm.getCategoryStep())
                        .tenant(tenant)
                        .build());
        document.setDocumentStatus(DocumentStatus.TO_PROCESS);
        document.setDocumentDeniedReasons(null);
        document.setDocumentSubCategory(documentSubCategory);
        if (documentFinancialForm.getMonthlySum() != null && documentFinancialForm.getMonthlySum() > 0
                && documentFinancialForm.getTypeDocumentFinancial() != DocumentSubCategory.NO_INCOME) {
            document.setMonthlySum(documentFinancialForm.getMonthlySum());
        } else {
            document.setMonthlySum(0);
        }

        if (document.getNoDocument() != null && !document.getNoDocument() && documentFinancialForm.getNoDocument()) {
            deleteFilesIfExistedBefore(document);
        }
        document.setNoDocument(documentFinancialForm.getNoDocument());
        documentRepository.save(document);

        if (Boolean.FALSE.equals(documentFinancialForm.getNoDocument())) {
            if (!documentFinancialForm.getDocuments().isEmpty()) {
                saveFiles(documentFinancialForm, document);
                document.setCustomText(null);
            } else {
                log.info("Refreshing info in [FINANCIAL] document with ID [" + documentFinancialForm.getId() + "]");
            }
        } else {
            document.setCustomText(documentFinancialForm.getCustomText());
        }
        documentRepository.save(document);
        tenant.lastUpdateDateProfile(LocalDateTime.now(), DocumentCategory.FINANCIAL);
        documentService.resetValidatedOrInProgressDocumentsAccordingCategories(tenant.getDocuments(), List.of(DocumentCategory.PROFESSIONAL, DocumentCategory.FINANCIAL, DocumentCategory.TAX));

        tenantStatusService.updateTenantStatus(tenant);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantRepository.save(tenant);
        return document;
    }

    private void deleteFilesIfExistedBefore(Document document) {
        documentHelperService.deleteFiles(document);
    }
}
