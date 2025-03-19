package fr.dossierfacile.api.front.partner.controller;

import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.enums.StepRegister;
import fr.dossierfacile.api.front.register.form.guarantor.natural_person.*;
import fr.dossierfacile.api.front.security.interfaces.ClientAuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.FinancialDocumentService;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.group.ApiPartner;
import fr.dossierfacile.common.config.ApiVersion;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.mapper.CategoriesMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api-partner/register/guarantorNaturalPerson", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiPartnerRegisterGuarantorNaturalPersonController {
    private final TenantService tenantService;
    private final ClientAuthenticationFacade clientAuthenticationFacade;
    private final Validator validator;
    private final CategoriesMapper categoriesMapper;
    private final FinancialDocumentService financialDocumentService;

    private void validate(Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void convert(UserApi userApi, DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm) {
        documentResidencyGuarantorNaturalPersonForm.setTypeDocumentResidency(
                categoriesMapper.mapSubCategory(documentResidencyGuarantorNaturalPersonForm.getTypeDocumentResidency(), userApi));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentIdentification")
    public ResponseEntity<TenantModel> documentIdentification(@Validated(ApiPartner.class) DocumentIdentificationGuarantorNaturalPersonForm documentIdentificationGuarantorNaturalPersonForm) {
        var tenant = tenantService.findById(documentIdentificationGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentIdentificationGuarantorNaturalPersonForm, StepRegister.DOCUMENT_IDENTIFICATION_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationGuarantorNaturalPersonFileForm.tenantId)")
    @PostMapping("/documentIdentification/v2")
    public ResponseEntity<TenantModel> documentIdentificationFile(@Validated(ApiPartner.class) DocumentIdentificationGuarantorNaturalPersonFileForm documentIdentificationGuarantorNaturalPersonFileForm) {
        var tenant = tenantService.findById(documentIdentificationGuarantorNaturalPersonFileForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentIdentificationGuarantorNaturalPersonFileForm, StepRegister.DOCUMENT_IDENTIFICATION_GUARANTOR_NATURAL_PERSON_FILE);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#nameGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/name")
    public ResponseEntity<TenantModel> guarantorName(@Validated(ApiPartner.class) NameGuarantorNaturalPersonForm nameGuarantorNaturalPersonForm) {
        var tenant = tenantService.findById(nameGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, nameGuarantorNaturalPersonForm, StepRegister.NAME_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentResidencyGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentResidency")
    public ResponseEntity<TenantModel> documentResidency(@Validated(ApiPartner.class) DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm) {
        // Validate form according partner api version
        UserApi userApi = clientAuthenticationFacade.getClient();
        validate(documentResidencyGuarantorNaturalPersonForm, ApiPartner.class, ApiVersion.getVersionClass(userApi.getVersion()));
        convert(userApi, documentResidencyGuarantorNaturalPersonForm);
        Tenant tenant = tenantService.findById(documentResidencyGuarantorNaturalPersonForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentResidencyGuarantorNaturalPersonForm, StepRegister.DOCUMENT_RESIDENCY_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentProfessionalGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentProfessional")
    public ResponseEntity<TenantModel> documentProfessional(@Validated(ApiPartner.class) DocumentProfessionalGuarantorNaturalPersonForm documentProfessionalGuarantorNaturalPersonForm) {
        var tenant = tenantService.findById(documentProfessionalGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentProfessionalGuarantorNaturalPersonForm, StepRegister.DOCUMENT_PROFESSIONAL_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentFinancialGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentFinancial")
    public ResponseEntity<TenantModel> documentFinancial(@Validated(ApiPartner.class) DocumentFinancialGuarantorNaturalPersonForm documentFinancialGuarantorNaturalPersonForm) {
        financialDocumentService.setFinancialDocumentCategoryStep(documentFinancialGuarantorNaturalPersonForm);
        var tenant = tenantService.findById(documentFinancialGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentFinancialGuarantorNaturalPersonForm, StepRegister.DOCUMENT_FINANCIAL_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }


    @PreAuthorize("hasPermissionOnTenant(#documentTaxGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentTax")
    public ResponseEntity<TenantModel> documentTax(@Validated(ApiPartner.class) DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm) {
        var tenant = tenantService.findById(documentTaxGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentTaxGuarantorNaturalPersonForm, StepRegister.DOCUMENT_TAX_GUARANTOR_NATURAL_PERSON);
        return ok(tenantModel);
    }
}
