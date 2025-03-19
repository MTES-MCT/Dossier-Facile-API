package fr.dossierfacile.api.front.controller;

import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.enums.StepRegister;
import fr.dossierfacile.api.front.register.form.tenant.*;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.group.Dossier;
import fr.dossierfacile.api.front.validator.group.FinancialDocumentGroup;
import fr.dossierfacile.api.front.validator.group.ResidencyDocumentGroup;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.service.interfaces.LogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
    private final AuthenticationFacade authenticationFacade;
    private final LogService logService;

    @PreAuthorize("hasPermissionOnTenant(#namesForm.tenantId)")
    @PostMapping(value = "/names", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> names(@Validated(Dossier.class) @RequestBody NamesForm namesForm) {
        var tenant = authenticationFacade.getTenant(namesForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, namesForm, StepRegister.NAMES);
        logService.saveStepLog(tenantModel.getId(), StepRegister.NAMES.getClazz().getSimpleName());
        Tenant loggedTenant = (namesForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PostMapping(value = "/application/v2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> application(@Validated(Dossier.class) @RequestBody ApplicationFormV2 applicationForm) {
        Tenant tenant = authenticationFacade.getTenant(applicationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, applicationForm, StepRegister.APPLICATION);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        Tenant loggedTenant = (applicationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#honorDeclarationForm.tenantId)")
    @PostMapping(value = "/honorDeclaration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> honorDeclaration(@Validated(Dossier.class) @RequestBody HonorDeclarationForm honorDeclarationForm) {
        Tenant tenant = authenticationFacade.getTenant(honorDeclarationForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, honorDeclarationForm, StepRegister.HONOR_DECLARATION);
        logService.saveLog(LogType.ACCOUNT_COMPLETED, tenantModel.getId());
        Tenant loggedTenant = (honorDeclarationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationForm.tenantId)")
    @PostMapping(value = "/documentIdentification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentIdentification(@Validated(Dossier.class) DocumentIdentificationForm documentIdentificationForm) {
        Tenant tenant = authenticationFacade.getTenant(documentIdentificationForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentIdentificationForm, StepRegister.DOCUMENT_IDENTIFICATION);
        Tenant loggedTenant = (documentIdentificationForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentResidencyForm.tenantId)")
    @PostMapping(value = "/documentResidency", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentResidency(@Validated({Dossier.class, ResidencyDocumentGroup.class}) DocumentResidencyForm documentResidencyForm) {
        Tenant tenant = authenticationFacade.getTenant(documentResidencyForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentResidencyForm, StepRegister.DOCUMENT_RESIDENCY);
        Tenant loggedTenant = (documentResidencyForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentProfessionalForm.tenantId)")
    @PostMapping(value = "/documentProfessional", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentProfessional(@Validated(Dossier.class) DocumentProfessionalForm documentProfessionalForm) {
        Tenant tenant = authenticationFacade.getTenant(documentProfessionalForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentProfessionalForm, StepRegister.DOCUMENT_PROFESSIONAL);
        Tenant loggedTenant = (documentProfessionalForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentFinancialForm.tenantId)")
    @PostMapping(value = "/documentFinancial", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register a financial document", notes = "Save a financial document for a tenant")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document has been saved", response = TenantModel.class),
            @ApiResponse(code = 403, message = "Forbidden: User not verified"),
            @ApiResponse(code = 400, message = "Wrong request params")
    })
    public ResponseEntity<TenantModel> documentFinancial(@Validated({Dossier.class, FinancialDocumentGroup.class}) DocumentFinancialForm documentFinancialForm) {
        Tenant tenant = authenticationFacade.getTenant(documentFinancialForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentFinancialForm, StepRegister.DOCUMENT_FINANCIAL);
        Tenant loggedTenant = (documentFinancialForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#documentTaxForm.tenantId)")
    @PostMapping(value = "/documentTax", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> documentTax(@Validated(Dossier.class) DocumentTaxForm documentTaxForm) {
        Tenant tenant = authenticationFacade.getTenant(documentTaxForm.getTenantId());
        tenantService.saveStepRegister(tenant, documentTaxForm, StepRegister.DOCUMENT_TAX);
        Tenant loggedTenant = (documentTaxForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }

    @PreAuthorize("hasPermissionOnTenant(#guarantorTypeForm.tenantId)")
    @PostMapping(value = "/guarantorType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> guarantor(@RequestBody @Validated(Dossier.class) GuarantorTypeForm guarantorTypeForm) {
        Tenant tenant = authenticationFacade.getTenant(guarantorTypeForm.getTenantId());
        TenantModel tenantModel = tenantService.saveStepRegister(tenant, guarantorTypeForm, StepRegister.GUARANTOR_TYPE);
        Tenant loggedTenant = (guarantorTypeForm.getTenantId() == null) ? tenant : authenticationFacade.getLoggedTenant();
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());

        return ok(tenantMapper.toTenantModel(loggedTenant, null));
    }
}
