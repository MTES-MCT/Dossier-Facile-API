package fr.dossierfacile.api.front.validator.guarantor.natural_person.tax;

import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentTaxGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.anotation.guarantor.natural_person.tax.NumberOfDocumentTaxGuarantorNaturalPerson;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class NumberOfDocumentTaxGuarantorNaturalPersonValidator implements ConstraintValidator<NumberOfDocumentTaxGuarantorNaturalPerson, DocumentTaxGuarantorNaturalPersonForm> {

    private static final String DOCUMENTS = "documents";
    private static final String RESPONSE = "number of document must be less than 15";

    private final TenantService tenantService;
    private final FileRepository fileRepository;

    @Override
    public void initialize(NumberOfDocumentTaxGuarantorNaturalPerson constraintAnnotation) {
        //this method is empty
    }

    @Override
    public boolean isValid(DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm, ConstraintValidatorContext constraintValidatorContext) {
        Tenant tenant = tenantService.findById(documentTaxGuarantorNaturalPersonForm.getTenantId());
        long countOld = fileRepository.countFileByDocumentCategoryGuarantorIdTypeGuarantorTenant(
                DocumentCategory.TAX,
                documentTaxGuarantorNaturalPersonForm.getGuarantorId(),
                TypeGuarantor.NATURAL_PERSON,
                tenant
        );
        long countNew = documentTaxGuarantorNaturalPersonForm.getDocuments().stream().filter(f -> !f.isEmpty()).count();

        boolean isValid;
        if (documentTaxGuarantorNaturalPersonForm.getTypeDocumentTax() == DocumentSubCategory.MY_NAME) {
            isValid = 1 <= countNew + countOld && countNew + countOld <= 5;
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(RESPONSE)
                        .addPropertyNode(DOCUMENTS).addConstraintViolation();
            }
        } else if (documentTaxGuarantorNaturalPersonForm.getTypeDocumentTax() == DocumentSubCategory.OTHER_TAX
                && !documentTaxGuarantorNaturalPersonForm.getNoDocument()) {
            isValid = 1 <= countNew + countOld && countNew + countOld <= 5;
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(RESPONSE)
                        .addPropertyNode(DOCUMENTS).addConstraintViolation();
            }
        } else {
            isValid = countNew + countOld == 0;
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("number of document must be 0")
                        .addPropertyNode(DOCUMENTS).addConstraintViolation();
            }
        }
        return isValid;
    }
}
