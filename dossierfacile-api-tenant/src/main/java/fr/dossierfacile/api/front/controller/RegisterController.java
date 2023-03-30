package fr.dossierfacile.api.front.controller;

import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.enums.StepRegister;
import fr.dossierfacile.api.front.register.form.tenant.AccountForm;
import fr.dossierfacile.api.front.register.form.tenant.ApplicationForm;
import fr.dossierfacile.api.front.register.form.tenant.ApplicationFormV2;
import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentIdentificationForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentProfessionalForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentResidencyForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentTaxForm;
import fr.dossierfacile.api.front.register.form.tenant.GuarantorTypeForm;
import fr.dossierfacile.api.front.register.form.tenant.HonorDeclarationForm;
import fr.dossierfacile.api.front.register.form.tenant.NamesForm;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.service.interfaces.UserService;
import fr.dossierfacile.api.front.validator.group.Dossier;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.service.interfaces.LogService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@RequestMapping("/api/register")
public class RegisterController {

    private final TenantMapper tenantMapper;
    private final TenantService tenantService;
    private final UserService userService;
    private final AuthenticationFacade authenticationFacade;
    private final LogService logService;

    @PostMapping(value = "/account", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> account(@Validated(Dossier.class) @RequestBody AccountForm accountForm) {
        TenantModel tenantModel = tenantService.saveStepRegister(null, accountForm, StepRegister.ACCOUNT);
        logService.saveLog(LogType.ACCOUNT_CREATED, tenantModel.getId());
        return ok(tenantModel);
    }

    @GetMapping("/confirmAccount/{token}")
    public ResponseEntity<Void> confirmAccount(@PathVariable String token) {
        long tenantId = userService.confirmAccount(token);
        logService.saveLog(LogType.EMAIL_ACCOUNT_VALIDATED, tenantId);
        return ok().build();
    }

    @PreAuthorize("hasPermissionOnTenant(#namesForm.tenantId)")
    @PostMapping(value = "/names", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> names(@Validated(Dossier.class) @RequestBody NamesForm namesForm) {
        var tenant = authenticationFacade.getTenant(namesForm.getTenantId() );
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, namesForm, StepRegister.NAMES);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (namesForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @Deprecated
    @PostMapping(value = "/application", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> application(@Validated(Dossier.class) @RequestBody ApplicationForm applicationForm) {
        Tenant tenant = authenticationFacade.getTenant(applicationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, applicationForm, StepRegister.APPLICATION_V1);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (applicationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PostMapping(value = "/application/v2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> application(@Validated(Dossier.class) @RequestBody ApplicationFormV2 applicationForm) {
        Tenant tenant = authenticationFacade.getTenant(applicationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, applicationForm, StepRegister.APPLICATION);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (applicationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#honorDeclarationForm.tenantId)")
    @PostMapping(value = "/honorDeclaration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> honorDeclaration(@Validated(Dossier.class) @RequestBody HonorDeclarationForm honorDeclarationForm) {
        Tenant tenant = authenticationFacade.getTenant(honorDeclarationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, honorDeclarationForm, StepRegister.HONOR_DECLARATION);
        logService.saveLog(LogType.ACCOUNT_COMPLETED, tenantModel.getId());
        Tenant loggedTenant = (honorDeclarationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationForm.tenantId)")
    @PostMapping(value = "/documentIdentification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentIdentification(@Validated(Dossier.class) DocumentIdentificationForm documentIdentificationForm) {
        Tenant tenant = authenticationFacade.getTenant(documentIdentificationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentIdentificationForm, StepRegister.DOCUMENT_IDENTIFICATION);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (documentIdentificationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentResidencyForm.tenantId)")
    @PostMapping(value = "/documentResidency", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentResidency(@Validated(Dossier.class) DocumentResidencyForm documentResidencyForm) {
        Tenant tenant = authenticationFacade.getTenant(documentResidencyForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentResidencyForm, StepRegister.DOCUMENT_RESIDENCY);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (documentResidencyForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentProfessionalForm.tenantId)")
    @PostMapping(value = "/documentProfessional", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentProfessional(@Validated(Dossier.class) DocumentProfessionalForm documentProfessionalForm) {
        Tenant tenant = authenticationFacade.getTenant(documentProfessionalForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentProfessionalForm, StepRegister.DOCUMENT_PROFESSIONAL);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (documentProfessionalForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentFinancialForm.tenantId)")
    @PostMapping(value = "/documentFinancial", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentFinancial(@Validated(Dossier.class) DocumentFinancialForm documentFinancialForm) {
        Tenant tenant = authenticationFacade.getTenant(documentFinancialForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentFinancialForm, StepRegister.DOCUMENT_FINANCIAL);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (documentFinancialForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentTaxForm.tenantId)")
    @PostMapping(value = "/documentTax", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentTax(@Validated(Dossier.class) DocumentTaxForm documentTaxForm) {
        Tenant tenant = authenticationFacade.getTenant(documentTaxForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, documentTaxForm, StepRegister.DOCUMENT_TAX);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (documentTaxForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant));
    }

    @PreAuthorize("hasPermissionOnTenant(#guarantorTypeForm.tenantId)")
    @PostMapping(value = "/guarantorType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> guarantor(@RequestBody @Validated(Dossier.class) GuarantorTypeForm guarantorTypeForm) {
        Tenant tenant = authenticationFacade.getTenant(guarantorTypeForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, guarantorTypeForm, StepRegister.GUARANTOR_TYPE);
        Tenant loggedTenant = (guarantorTypeForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());

        return ok(tenantMapper.toTenantModel(loggedTenant));
    }
}
