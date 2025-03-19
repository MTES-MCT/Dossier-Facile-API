package fr.dossierfacile.api.front.validator;

import fr.dossierfacile.api.front.register.form.IDocumentFinancialForm;
import fr.dossierfacile.api.front.validator.annotation.FinancialDocument;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static fr.dossierfacile.common.enums.DocumentCategoryStep.*;

public class FinancialDocumentCategoriesValidator extends AbstractDocumentCategoriesValidator implements ConstraintValidator<FinancialDocument, IDocumentFinancialForm> {

    @Override
    public void initialize(FinancialDocument constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(IDocumentFinancialForm documentFinancialForm, ConstraintValidatorContext constraintValidatorContext) {
        switch (documentFinancialForm.getTypeDocumentFinancial()) {
            case SALARY -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        SALARY_EMPLOYED_LESS_3_MONTHS,
                        SALARY_EMPLOYED_MORE_3_MONTHS,
                        SALARY_EMPLOYED_NOT_YET,
                        SALARY_FREELANCE_AUTOENTREPRENEUR,
                        SALARY_FREELANCE_OTHER,
                        SALARY_INTERMITTENT,
                        SALARY_ARTIST_AUTHOR,
                        SALARY_UNKNOWN
                );
                return verifyMandatoryStep(
                        DocumentSubCategory.SALARY,
                        documentFinancialForm.getCategoryStep(),
                        listOfAllowedSubTypes,
                        constraintValidatorContext
                );
            }
            case SOCIAL_SERVICE -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        SOCIAL_SERVICE_CAF_LESS_3_MONTHS,
                        SOCIAL_SERVICE_CAF_MORE_3_MONTHS,
                        SOCIAL_SERVICE_FRANCE_TRAVAIL_LESS_3_MONTHS,
                        SOCIAL_SERVICE_FRANCE_TRAVAIL_MORE_3_MONTHS,
                        SOCIAL_SERVICE_FRANCE_TRAVAIL_NOT_YET,
                        SOCIAL_SERVICE_APL_LESS_3_MONTHS,
                        SOCIAL_SERVICE_APL_MORE_3_MONTHS,
                        SOCIAL_SERVICE_APL_NOT_YET,
                        SOCIAL_SERVICE_AAH_LESS_3_MONTHS,
                        SOCIAL_SERVICE_AAH_MORE_3_MONTHS,
                        SOCIAL_SERVICE_AAH_NOT_YET,
                        SOCIAL_SERVICE_OTHER
                );
                return verifyMandatoryStep(
                        DocumentSubCategory.SOCIAL_SERVICE,
                        documentFinancialForm.getCategoryStep(),
                        listOfAllowedSubTypes,
                        constraintValidatorContext
                );
            }
            case RENT -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        RENT_RENTAL_RECEIPT,
                        RENT_RENTAL_NO_RECEIPT,
                        RENT_ANNUITY_LIFE,
                        RENT_OTHER
                );
                return verifyMandatoryStep(
                        DocumentSubCategory.RENT,
                        documentFinancialForm.getCategoryStep(),
                        listOfAllowedSubTypes,
                        constraintValidatorContext
                );
            }
            case PENSION -> {
                List<DocumentCategoryStep> listOfAllowedSubTypes = List.of(
                        PENSION_STATEMENT,
                        PENSION_NO_STATEMENT,
                        PENSION_DISABILITY_LESS_3_MONTHS,
                        PENSION_DISABILITY_MORE_3_MONTHS,
                        PENSION_DISABILITY_NOT_YET,
                        PENSION_ALIMONY,
                        PENSION_UNKNOWN
                );
                return verifyMandatoryStep(
                        DocumentSubCategory.PENSION,
                        documentFinancialForm.getCategoryStep(),
                        listOfAllowedSubTypes,
                        constraintValidatorContext
                );
            }
            case SCHOLARSHIP, NO_INCOME -> {
                return handleNoCategoryStep(documentFinancialForm.getCategoryStep(), documentFinancialForm.getTypeDocumentFinancial(), constraintValidatorContext);
            }
            default -> {
                return handleDefaultValidationError(documentFinancialForm.getTypeDocumentFinancial(), constraintValidatorContext);
            }
        }
    }
}
