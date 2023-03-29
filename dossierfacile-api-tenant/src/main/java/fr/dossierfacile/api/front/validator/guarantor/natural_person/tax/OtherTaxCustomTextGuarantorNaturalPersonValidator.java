package fr.dossierfacile.api.front.validator.guarantor.natural_person.tax;

import fr.dossierfacile.api.front.register.form.guarantor.natural_person.DocumentTaxGuarantorNaturalPersonForm;
import fr.dossierfacile.api.front.validator.anotation.guarantor.natural_person.tax.OtherTaxCustomTextGuarantorNaturalPerson;
import fr.dossierfacile.common.enums.DocumentSubCategory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OtherTaxCustomTextGuarantorNaturalPersonValidator implements ConstraintValidator<OtherTaxCustomTextGuarantorNaturalPerson, DocumentTaxGuarantorNaturalPersonForm> {

    @Override
    public boolean isValid(DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;
        if (documentTaxGuarantorNaturalPersonForm.getTypeDocumentTax() == DocumentSubCategory.OTHER_TAX && documentTaxGuarantorNaturalPersonForm.getNoDocument()) {
            isValid = documentTaxGuarantorNaturalPersonForm.getCustomText() != null && !documentTaxGuarantorNaturalPersonForm.getCustomText().isBlank();
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{javax.validation.constraints.NotEmpty.message}")
                        .addPropertyNode("customText").addConstraintViolation();
            }
        }
        return isValid;
    }
}
