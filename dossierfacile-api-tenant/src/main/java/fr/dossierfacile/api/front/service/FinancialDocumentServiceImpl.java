package fr.dossierfacile.api.front.service;

import fr.dossierfacile.api.front.register.form.IDocumentFinancialForm;
import fr.dossierfacile.api.front.service.interfaces.FinancialDocumentService;
import fr.dossierfacile.api.front.validator.group.FinancialDocumentGroup;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class FinancialDocumentServiceImpl implements FinancialDocumentService {

    private final Validator validator;

    private void validate(Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @Override
    public void setFinancialDocumentCategoryStep(IDocumentFinancialForm documentFinancialForm) {
        // If a categoryStep is already set, we validate it
        if (documentFinancialForm.getCategoryStep() != null) {
            validate(documentFinancialForm, FinancialDocumentGroup.class);
        } else {
            // Else we set the categoryStep according to the type of document
            switch (documentFinancialForm.getTypeDocumentFinancial()) {
                case SALARY:
                    documentFinancialForm.setCategoryStep(DocumentCategoryStep.SALARY_UNKNOWN);
                    break;
                case SOCIAL_SERVICE:
                    documentFinancialForm.setCategoryStep(DocumentCategoryStep.SOCIAL_SERVICE_OTHER);
                    break;
                case RENT:
                    documentFinancialForm.setCategoryStep(DocumentCategoryStep.RENT_OTHER);
                    break;
                case PENSION:
                    documentFinancialForm.setCategoryStep(DocumentCategoryStep.PENSION_UNKNOWN);
                    break;
                default:
                    documentFinancialForm.setCategoryStep(null);
                    break;
            }
        }
    }
}
