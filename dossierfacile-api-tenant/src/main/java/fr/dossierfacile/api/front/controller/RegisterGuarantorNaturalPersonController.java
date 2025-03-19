package fr.dossierfacile.api.front.controller;

import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.enums.StepRegister;
import fr.dossierfacile.api.front.register.form.guarantor.natural_person.*;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.group.Dossier;
import fr.dossierfacile.api.front.validator.group.FinancialDocumentGroup;
import fr.dossierfacile.api.front.validator.group.ResidencyDocumentGroup;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.service.interfaces.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/register/guarantorNaturalPerson", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class RegisterGuarantorNaturalPersonController {
    private final TenantService tenantService;
    private final TenantMapper tenantMapper;
    private final AuthenticationFacade authenticationFacade;
    private final LogService logService;

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentIdentification")
    public ResponseEntity<TenantModel> documentIdentification(@Validated(Dossier.class) DocumentIdentificationGuarantorNaturalPersonForm documentIdentificationGuarantorNaturalPersonForm) {
        var tenant = authenticationFacade.getTenant(documentIdentificationGuarantorNaturalPersonForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentIdentificationGuarantorNaturalPersonForm, StepRegister.DOCUMENT_IDENTIFICATION_GUARANTOR_NATURAL_PERSON);
        Tenant loggedTenant = (documentIdentificationGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationGuarantorNaturalPersonFileForm.tenantId)")
    @PostMapping("/documentIdentification/v2")
    public ResponseEntity<TenantModel> documentIdentificationFile(@Validated(Dossier.class) DocumentIdentificationGuarantorNaturalPersonFileForm documentIdentificationGuarantorNaturalPersonFileForm) {
        var tenant = authenticationFacade.getTenant(documentIdentificationGuarantorNaturalPersonFileForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentIdentificationGuarantorNaturalPersonFileForm, StepRegister.DOCUMENT_IDENTIFICATION_GUARANTOR_NATURAL_PERSON_FILE);
        Tenant loggedTenant = (documentIdentificationGuarantorNaturalPersonFileForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#nameGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/name")
    public ResponseEntity<TenantModel> guarantorName(NameGuarantorNaturalPersonForm nameGuarantorNaturalPersonForm) {
        var tenant = authenticationFacade.getTenant(nameGuarantorNaturalPersonForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, nameGuarantorNaturalPersonForm, StepRegister.NAME_GUARANTOR_NATURAL_PERSON);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (nameGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentResidencyGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentResidency")
    public ResponseEntity<TenantModel> documentResidency(@Validated({Dossier.class, ResidencyDocumentGroup.class}) DocumentResidencyGuarantorNaturalPersonForm documentResidencyGuarantorNaturalPersonForm) {
        Tenant tenant = authenticationFacade.getTenant(documentResidencyGuarantorNaturalPersonForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentResidencyGuarantorNaturalPersonForm, StepRegister.DOCUMENT_RESIDENCY_GUARANTOR_NATURAL_PERSON);
        Tenant loggedTenant = (documentResidencyGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentProfessionalGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentProfessional")
    public ResponseEntity<TenantModel> documentProfessional(@Validated(Dossier.class) DocumentProfessionalGuarantorNaturalPersonForm documentProfessionalGuarantorNaturalPersonForm) {
        var tenant = authenticationFacade.getTenant(documentProfessionalGuarantorNaturalPersonForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentProfessionalGuarantorNaturalPersonForm, StepRegister.DOCUMENT_PROFESSIONAL_GUARANTOR_NATURAL_PERSON);
        Tenant loggedTenant = (documentProfessionalGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentFinancialGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentFinancial")
    public ResponseEntity<TenantModel> documentFinancial(@Validated({Dossier.class, FinancialDocumentGroup.class}) DocumentFinancialGuarantorNaturalPersonForm documentFinancialGuarantorNaturalPersonForm) {
        var tenant = authenticationFacade.getTenant(documentFinancialGuarantorNaturalPersonForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentFinancialGuarantorNaturalPersonForm, StepRegister.DOCUMENT_FINANCIAL_GUARANTOR_NATURAL_PERSON);
        Tenant loggedTenant = (documentFinancialGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentTaxGuarantorNaturalPersonForm.tenantId)")
    @PostMapping("/documentTax")
    public ResponseEntity<TenantModel> documentTax(@Validated(Dossier.class) DocumentTaxGuarantorNaturalPersonForm documentTaxGuarantorNaturalPersonForm) {
        var tenant = authenticationFacade.getTenant(documentTaxGuarantorNaturalPersonForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentTaxGuarantorNaturalPersonForm, StepRegister.DOCUMENT_TAX_GUARANTOR_NATURAL_PERSON);
        Tenant loggedTenant = (documentTaxGuarantorNaturalPersonForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }
}
