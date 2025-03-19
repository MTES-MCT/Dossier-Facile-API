package fr.dossierfacile.api.front.validator;

import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public abstract class AbstractDocumentCategoriesValidator {

    protected static final String VALIDATION_PROPERTY_NODE_NAME = "categoryStep";

    protected boolean verifyMandatoryStep(
            DocumentSubCategory subCategory,
            DocumentCategoryStep categoryStep,
            List<DocumentCategoryStep> availableStep,
            ConstraintValidatorContext constraintValidatorContext) {
        if (categoryStep == null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("For document sub category " + subCategory.name() + " category step can not be null")
                    .addPropertyNode(VALIDATION_PROPERTY_NODE_NAME)
                    .addConstraintViolation();
            return false;
        }
        if (!availableStep.contains(categoryStep)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(categoryStep.name() + " is not valid for document sub category " + subCategory.name())
                    .addPropertyNode(VALIDATION_PROPERTY_NODE_NAME)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    protected boolean handleNoCategoryStep(DocumentCategoryStep categoryStep, DocumentSubCategory subCategory, ConstraintValidatorContext constraintValidatorContext) {
        if (categoryStep != null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("For document sub category " + subCategory + " category step has to be null")
                    .addPropertyNode(VALIDATION_PROPERTY_NODE_NAME)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    protected boolean handleDefaultValidationError(DocumentSubCategory subCategory, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(subCategory + " is not a valid sub category")
                .addPropertyNode(VALIDATION_PROPERTY_NODE_NAME)
                .addConstraintViolation();
        return false;
    }
}
