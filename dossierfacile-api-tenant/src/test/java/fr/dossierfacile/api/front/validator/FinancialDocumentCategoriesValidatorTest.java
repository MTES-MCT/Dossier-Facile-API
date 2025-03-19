package fr.dossierfacile.api.front.validator;

import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FinancialDocumentCategoriesValidatorTest {

    private final FinancialDocumentCategoriesValidator validator = new FinancialDocumentCategoriesValidator();

    record ValidatorTestParam(DocumentSubCategory subCategory, DocumentCategoryStep step, String expectedError,
                              Boolean result) {
    }

    static Stream<Arguments> provideArgumentsForTest() {
        return Stream.of(
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SALARY,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SALARY,
                        null,
                        "For document sub category " + DocumentSubCategory.SALARY + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SALARY,
                        DocumentCategoryStep.GUEST_PROOF,
                        DocumentCategoryStep.GUEST_PROOF + " is not valid for document sub category " + DocumentSubCategory.SALARY,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SOCIAL_SERVICE,
                        DocumentCategoryStep.SOCIAL_SERVICE_CAF_LESS_3_MONTHS,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SOCIAL_SERVICE,
                        null,
                        "For document sub category " + DocumentSubCategory.SOCIAL_SERVICE + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SOCIAL_SERVICE,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS + " is not valid for document sub category " + DocumentSubCategory.SOCIAL_SERVICE,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.RENT,
                        DocumentCategoryStep.RENT_RENTAL_RECEIPT,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.RENT,
                        null,
                        "For document sub category " + DocumentSubCategory.RENT + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.RENT,
                        DocumentCategoryStep.SOCIAL_SERVICE_APL_NOT_YET,
                        DocumentCategoryStep.SOCIAL_SERVICE_APL_NOT_YET + " is not valid for document sub category " + DocumentSubCategory.RENT,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.PENSION,
                        DocumentCategoryStep.PENSION_DISABILITY_LESS_3_MONTHS,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.PENSION,
                        null,
                        "For document sub category " + DocumentSubCategory.PENSION + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.PENSION,
                        DocumentCategoryStep.RENT_RENTAL_RECEIPT,
                        DocumentCategoryStep.RENT_RENTAL_RECEIPT + " is not valid for document sub category " + DocumentSubCategory.PENSION,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SCHOLARSHIP,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.SCHOLARSHIP,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS,
                        "For document sub category " +  DocumentSubCategory.SCHOLARSHIP + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.NO_INCOME,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.NO_INCOME,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS,
                        "For document sub category " +  DocumentSubCategory.NO_INCOME + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.DRIVERS_LICENSE,
                        DocumentCategoryStep.SALARY_EMPLOYED_LESS_3_MONTHS,
                        DocumentSubCategory.DRIVERS_LICENSE + " is not a valid sub category",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        DocumentSubCategory.DRIVERS_LICENSE,
                        null,
                        DocumentSubCategory.DRIVERS_LICENSE + " is not a valid sub category",
                        false
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTest")
    void validationTest(ValidatorTestParam validatorTestParam) {
        // Given
        var validationContext = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder mockBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setTypeDocumentFinancial(validatorTestParam.subCategory);
        documentFinancialForm.setCategoryStep(validatorTestParam.step);

        when(mockBuilder.addPropertyNode(any())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));

        when(validationContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockBuilder);

        boolean result = validator.isValid(documentFinancialForm, validationContext);

        if (validatorTestParam.expectedError != null) {
            verify(validationContext, times(1)).buildConstraintViolationWithTemplate(validatorTestParam.expectedError);
        } else {
            verify(validationContext, times(0)).buildConstraintViolationWithTemplate(any());
        }

        assertThat(result).isEqualTo(validatorTestParam.result);
    }


}
