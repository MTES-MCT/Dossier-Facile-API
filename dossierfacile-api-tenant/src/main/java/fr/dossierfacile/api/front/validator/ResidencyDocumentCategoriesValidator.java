package fr.dossierfacile.api.front.validator;

import fr.dossierfacile.api.front.register.form.IDocumentResidencyForm;
import fr.dossierfacile.api.front.validator.annotation.ResidencyDocument;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static fr.dossierfacile.common.enums.DocumentCategoryStep.*;

public class ResidencyDocumentCategoriesValidator extends AbstractDocumentCategoriesValidator implements ConstraintValidator<ResidencyDocument, IDocumentResidencyForm> {

    private boolean isGuarantorMode;

    public ResidencyDocumentCategoriesValidator() {
        isGuarantorMode = false;
    }

    public ResidencyDocumentCategoriesValidator(boolean isGuarantorMode) {
        this.isGuarantorMode = isGuarantorMode;
    }

    @Override
    public void initialize(ResidencyDocument constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        isGuarantorMode = constraintAnnotation.isGuarantorMode();
    }

    @Override
    public boolean isValid(IDocumentResidencyForm documentResidencyForm, ConstraintValidatorContext constraintValidatorContext) {
        switch (documentResidencyForm.getTypeDocumentResidency()) {
            case TENANT -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        TENANT_PROOF,
                        TENANT_RECEIPT
                );
                if (!isGuarantorMode) {
                    return verifyMandatoryStep(
                            DocumentSubCategory.TENANT,
                            documentResidencyForm.getCategoryStep(),
                            listOfAllowedSubTypes,
                            constraintValidatorContext
                    );
                } else {
                    return handleNoCategoryStep(documentResidencyForm.getCategoryStep(), documentResidencyForm.getTypeDocumentResidency(), constraintValidatorContext);
                }
            }
            case GUEST -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        GUEST_PROOF,
                        GUEST_NO_PROOF
                );
                return verifyMandatoryStep(
                        DocumentSubCategory.GUEST,
                        documentResidencyForm.getCategoryStep(),
                        listOfAllowedSubTypes,
                        constraintValidatorContext
                );
            }
            case OWNER, GUEST_COMPANY, GUEST_ORGANISM, SHORT_TERM_RENTAL, OTHER_RESIDENCY -> {
                return handleNoCategoryStep(documentResidencyForm.getCategoryStep(), documentResidencyForm.getTypeDocumentResidency(), constraintValidatorContext);
            }
            default -> {
                return handleDefaultValidationError(documentResidencyForm.getTypeDocumentResidency(), constraintValidatorContext);
            }
        }
    }
}
