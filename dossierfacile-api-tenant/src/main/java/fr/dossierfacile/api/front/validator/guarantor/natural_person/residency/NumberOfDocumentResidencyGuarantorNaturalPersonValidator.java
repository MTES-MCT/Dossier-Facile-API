package fr.dossierfacile.api.front.validator.guarantor.natural_person.residency;

import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentResidencyGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.validator.TenantConstraintValidator;
import fr.dossierfacile.api.front.validator.annotation.guarantor.natural_person.residency.NumberOfDocumentResidencyGuarantorNaturalPerson;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NumberOfDocumentResidencyGuarantorNaturalPersonValidator extends TenantConstraintValidator<NumberOfDocumentResidencyGuarantorNaturalPerson, DocumentResidencyGuarantorNaturalPersonForm> {

    private final FileRepository fileRepository;

    @Override
    public boolean isValid(DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm, ConstraintValidatorContext constraintValidatorContext) {
        List<MultipartFile> documents = documentResidencyGuarantorNaturalPersonForm.getDocuments();

        Tenant tenant = getTenant(documentResidencyGuarantorNaturalPersonForm);
        long sizeOldDoc = 0;
        long countOld = fileRepository.countFileByDocumentCategoryGuarantorIdTypeGuarantorTenant(
                DocumentCategory.RESIDENCY,
                documentResidencyGuarantorNaturalPersonForm.getGuarantorId(),
                TypeGuarantor.NATURAL_PERSON,
                tenant
        );
        long countNew = documents.stream()
                .filter(f -> !f.isEmpty())
                .count();
        if (countOld > 0) {
            sizeOldDoc = fileRepository.sumSizeOfAllFilesInDocumentForGuarantorTenant(
                    DocumentCategory.RESIDENCY,
                    documentResidencyGuarantorNaturalPersonForm.getGuarantorId(),
                    TypeGuarantor.NATURAL_PERSON,
                    tenant
            );
        }
        long sizeNewDoc = documents.stream().filter(o -> o.getSize() >= 0).mapToLong(MultipartFile::getSize).sum();

        int minNumberOfDocs = 1;
        if (documentResidencyGuarantorNaturalPersonForm.getTypeDocumentResidency() == DocumentSubCategory.OTHER_RESIDENCY 
            && StringUtils.isNotBlank(documentResidencyGuarantorNaturalPersonForm.getCustomText())) {
            minNumberOfDocs = 0;
        }

        return minNumberOfDocs <= countNew + countOld && countNew + countOld <= 10 && sizeNewDoc + sizeOldDoc <= 52428800;
    }
}
