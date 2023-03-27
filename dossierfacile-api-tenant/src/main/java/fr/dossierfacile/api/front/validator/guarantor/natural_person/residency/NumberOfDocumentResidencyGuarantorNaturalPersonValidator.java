package fr.dossierfacile.api.front.validator.guarantor.natural_person.residency;

import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentResidencyGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.anotation.guarantor.natural_person.residency.NumberOfDocumentResidencyGuarantorNaturalPerson;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class NumberOfDocumentResidencyGuarantorNaturalPersonValidator implements ConstraintValidator<NumberOfDocumentResidencyGuarantorNaturalPerson, DocumentResidencyGuarantorNaturalPersonForm> {

    private final TenantService tenantService;
    private final FileRepository fileRepository;

    @Override
    public void initialize(NumberOfDocumentResidencyGuarantorNaturalPerson constraintAnnotation) {
        //this method is empty
    }

    @Override
    public boolean isValid(DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm, ConstraintValidatorContext constraintValidatorContext) {
        Tenant tenant = tenantService.findById(documentResidencyGuarantorNaturalPersonForm.getTenantId());
        long sizeOldDoc = 0;
        long countOld = fileRepository.countFileByDocumentCategoryGuarantorIdTypeGuarantorTenant(
                DocumentCategory.RESIDENCY,
                documentResidencyGuarantorNaturalPersonForm.getGuarantorId(),
                TypeGuarantor.NATURAL_PERSON,
                tenant
        );
        long countNew = documentResidencyGuarantorNaturalPersonForm.getDocuments()
                .stream()
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
        long sizeNewDoc = documentResidencyGuarantorNaturalPersonForm.getDocuments().stream().filter(o -> o.getSize() >= 0).mapToLong(MultipartFile::getSize).sum();

        return 1 <= countNew + countOld && countNew + countOld <= 10 && sizeNewDoc + sizeOldDoc <= 52428800;
    }
}
