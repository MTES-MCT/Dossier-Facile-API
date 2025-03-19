package fr.dossierfacile.api.front.validator;

import fr.dossierfacile.api.front.register.form.tenant.DocumentResidencyForm;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ResidencyDocumentCategoriesValidatorTest {

    record ValidatorTestParam(
            boolean isGuarantorMode,
            DocumentSubCategory subCategory,
            DocumentCategoryStep step,
            String expectedError,
            Boolean result
    ) {}

    static Stream<Arguments> provideArgumentsForTest() {
        return Stream.of(
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.TENANT,
                        DocumentCategoryStep.TENANT_PROOF,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.TENANT,
                        null,
                        "For document sub category " + DocumentSubCategory.TENANT + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.TENANT,
                        DocumentCategoryStep.GUEST_PROOF,
                        DocumentCategoryStep.GUEST_PROOF + " is not valid for document sub category " + DocumentSubCategory.TENANT,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST,
                        DocumentCategoryStep.GUEST_PROOF,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST,
                        null,
                        "For document sub category " + DocumentSubCategory.GUEST + " category step can not be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST,
                        DocumentCategoryStep.TENANT_PROOF,
                        DocumentCategoryStep.TENANT_PROOF + " is not valid for document sub category " + DocumentSubCategory.GUEST,
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.OWNER,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.OWNER,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.OWNER + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST_COMPANY,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST_COMPANY,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.GUEST_COMPANY + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST_ORGANISM,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.GUEST_ORGANISM,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.GUEST_ORGANISM + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.SHORT_TERM_RENTAL,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.SHORT_TERM_RENTAL,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.SHORT_TERM_RENTAL + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.OTHER_RESIDENCY,
                        null,
                        null,
                        true
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.OTHER_RESIDENCY,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.OTHER_RESIDENCY + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.DRIVERS_LICENSE,
                        DocumentCategoryStep.TENANT_PROOF,
                        DocumentSubCategory.DRIVERS_LICENSE + " is not a valid sub category",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        false,
                        DocumentSubCategory.DRIVERS_LICENSE,
                        null,
                        DocumentSubCategory.DRIVERS_LICENSE + " is not a valid sub category",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        true,
                        DocumentSubCategory.TENANT,
                        DocumentCategoryStep.TENANT_PROOF,
                        "For document sub category " +  DocumentSubCategory.TENANT + " category step has to be null",
                        false
                )),
                Arguments.of(new ValidatorTestParam(
                        true,
                        DocumentSubCategory.TENANT,
                        null,
                        null,
                        true
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTest")
    void validationTest(ValidatorTestParam validatorTestParam) {

        ResidencyDocumentCategoriesValidator validator = new ResidencyDocumentCategoriesValidator(validatorTestParam.isGuarantorMode);
        // Given
        var validationContext = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder mockBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setTypeDocumentResidency(validatorTestParam.subCategory);
        documentResidencyForm.setCategoryStep(validatorTestParam.step);

        when(mockBuilder.addPropertyNode(any())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));

        when(validationContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockBuilder);

        boolean result = validator.isValid(documentResidencyForm, validationContext);

        if (validatorTestParam.expectedError != null) {
            verify(validationContext, times(1)).buildConstraintViolationWithTemplate(validatorTestParam.expectedError);
        } else {
            verify(validationContext, times(0)).buildConstraintViolationWithTemplate(any());
        }

        assertThat(result).isEqualTo(validatorTestParam.result);
    }
}