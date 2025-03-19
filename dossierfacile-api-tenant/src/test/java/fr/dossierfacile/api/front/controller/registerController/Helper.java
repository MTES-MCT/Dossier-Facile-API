package fr.dossierfacile.api.front.controller.registerController;

import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentResidencyForm;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;

public class Helper {

    public static DocumentFinancialForm invalidDocumentBecauseNoDocument() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setCategoryStep(DocumentCategoryStep.SALARY_EMPLOYED_NOT_YET);
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
        return documentFinancialForm;
    }

    public static DocumentResidencyForm invalidDocumentResidencyBecauseWrongType() {
        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setCategoryStep(DocumentCategoryStep.TENANT_RECEIPT);
        documentResidencyForm.setTypeDocumentResidency(DocumentSubCategory.SALARY);
        return documentResidencyForm;
    }

    public static DocumentResidencyForm invalidDocumentResidencyBecauseWrongStep() {
        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setCategoryStep(DocumentCategoryStep.GUEST_PROOF);
        documentResidencyForm.setTypeDocumentResidency(DocumentSubCategory.TENANT);
        return documentResidencyForm;
    }

    public static DocumentResidencyForm invalidDocumentResidencyForGuarantor() {
        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setCategoryStep(DocumentCategoryStep.TENANT_RECEIPT);
        documentResidencyForm.setTypeDocumentResidency(DocumentSubCategory.TENANT);
        return documentResidencyForm;
    }

    public static DocumentResidencyForm invalidDocumentResidencyBecauseNoStep() {
        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setCategoryStep(null);
        documentResidencyForm.setTypeDocumentResidency(DocumentSubCategory.TENANT);
        return documentResidencyForm;
    }

    public static DocumentResidencyForm invalidDocumentResidencyBecauseNoStepGuarantor() {
        DocumentResidencyForm documentResidencyForm = new DocumentResidencyForm();
        documentResidencyForm.setCategoryStep(null);
        documentResidencyForm.setTypeDocumentResidency(DocumentSubCategory.GUEST);
        return documentResidencyForm;
    }

    public static DocumentFinancialForm invalidDocumentBecauseWrongType() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setNoDocument(false);
        documentFinancialForm.setCategoryStep(DocumentCategoryStep.SALARY_EMPLOYED_NOT_YET);
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.MY_NAME);
        return documentFinancialForm;
    }

    public static DocumentFinancialForm invalidDocumentBecauseWrongStep() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setNoDocument(false);
        documentFinancialForm.setCategoryStep(DocumentCategoryStep.PENSION_NO_STATEMENT);
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
        return documentFinancialForm;
    }

    public static DocumentFinancialForm invalidDocumentBecauseNoStep() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setNoDocument(false);
        documentFinancialForm.setCategoryStep(DocumentCategoryStep.SALARY_EMPLOYED_NOT_YET);
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.SCHOLARSHIP);
        return documentFinancialForm;
    }

    public static DocumentFinancialForm invalidDocumentBecauseWrongStepPartner() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setNoDocument(true);
        documentFinancialForm.setTenantId(1L);
        documentFinancialForm.setCustomText("test");
        documentFinancialForm.setCategoryStep(DocumentCategoryStep.PENSION_NO_STATEMENT);
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
        return documentFinancialForm;
    }

    public static DocumentFinancialForm validDocumentForPartner() {
        DocumentFinancialForm documentFinancialForm = new DocumentFinancialForm();
        documentFinancialForm.setMonthlySum(1000);
        documentFinancialForm.setNoDocument(true);
        documentFinancialForm.setTenantId(1L);
        documentFinancialForm.setCustomText("test");
        documentFinancialForm.setTypeDocumentFinancial(DocumentSubCategory.SALARY);
        return documentFinancialForm;
    }


}
