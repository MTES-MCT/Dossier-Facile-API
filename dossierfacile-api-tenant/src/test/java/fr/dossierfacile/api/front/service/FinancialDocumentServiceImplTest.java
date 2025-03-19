package fr.dossierfacile.api.front.service;

import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ValidationAutoConfiguration.class, FinancialDocumentServiceImpl.class})
class FinancialDocumentServiceImplTest {

    @Autowired
    private FinancialDocumentServiceImpl financialDocumentService;

    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private AuthenticationFacade authenticationFacade;

    @MockBean
    private TenantService tenantService;

    @Nested
    class WhenNoCategoryStepIsSet {
        @Test
        void shouldSetCategoryStepToSalaryUnknown() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);

            assertThat(financialForm.getCategoryStep()).isEqualTo(DocumentCategoryStep.SALARY_UNKNOWN);
        }

        @Test
        void shouldSetCategoryStepToSocialServiceOther() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SOCIAL_SERVICE);
            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);

            assertThat(financialForm.getCategoryStep()).isEqualTo(DocumentCategoryStep.SOCIAL_SERVICE_OTHER);
        }

        @Test
        void shouldSetCategoryStepToRentOther() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.RENT);
            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);

            assertThat(financialForm.getCategoryStep()).isEqualTo(DocumentCategoryStep.RENT_OTHER);
        }

        @Test
        void shouldSetCategoryStepToPensionUnknown() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.PENSION);
            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);

            assertThat(financialForm.getCategoryStep()).isEqualTo(DocumentCategoryStep.PENSION_UNKNOWN);
        }

        @Test
        void shouldKeepCategoryStepNull() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SCHOLARSHIP);
            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);

            assertThat(financialForm.getCategoryStep()).isNull();
        }
    }

    @Nested
    class WhenCategoryStepIsSet {

        @Test
        void shouldReturnConstraintViolationException() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
            financialForm.setCategoryStep(DocumentCategoryStep.SALARY_EMPLOYED_NOT_YET);


            assertThrows(ConstraintViolationException.class, () -> financialDocumentService.setFinancialDocumentCategoryStep(financialForm));
        }

        @Test
        void shouldThrowConstrainViolationExceptionBecauseWrongStep() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
            financialForm.setCategoryStep(DocumentCategoryStep.SOCIAL_SERVICE_AAH_LESS_3_MONTHS);
            financialForm.setNoDocument(true);
            financialForm.setMonthlySum(1500);
            financialForm.setCustomText("test");


            assertThrows(ConstraintViolationException.class, () -> financialDocumentService.setFinancialDocumentCategoryStep(financialForm));
        }

        @Test
        void shouldBeValid() {
            var financialForm = new DocumentFinancialForm();
            financialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
            financialForm.setCategoryStep(DocumentCategoryStep.SALARY_EMPLOYED_NOT_YET);
            financialForm.setNoDocument(true);
            financialForm.setMonthlySum(1500);
            financialForm.setCustomText("test");

            financialDocumentService.setFinancialDocumentCategoryStep(financialForm);
        }
    }

}
