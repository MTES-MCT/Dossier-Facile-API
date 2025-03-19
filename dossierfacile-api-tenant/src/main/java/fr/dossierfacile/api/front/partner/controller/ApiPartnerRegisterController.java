package fr.dossierfacile.api.front.partner.controller;

import fr.dossierfacile.api.front.aop.annotation.MethodLogTime;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.enums.StepRegister;
import fr.dossierfacile.api.front.register.form.partner.AccountPartnerForm;
import fr.dossierfacile.api.front.register.form.tenant.*;
import fr.dossierfacile.api.front.security.interfaces.ClientAuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.FinancialDocumentService;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.FinancialDocumentCategoriesValidator;
import fr.dossierfacile.api.front.validator.group.ApiPartner;
import fr.dossierfacile.api.front.validator.group.FinancialDocumentGroup;
import fr.dossierfacile.common.config.ApiVersion;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.mapper.CategoriesMapper;
import fr.dossierfacile.common.service.interfaces.LogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api-partner/register", produces = MediaType.APPLICATION_JSON_VALUE)
@MethodLogTime
@Slf4j
public class ApiPartnerRegisterController {
    private final ClientAuthenticationFacade clientAuthenticationFacade;
    private final Validator validator;
    private final TenantService tenantService;
    private final LogService logService;
    private final CategoriesMapper categoriesMapper;
    private final FinancialDocumentService financialDocumentService;

    private void validate(Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void convert(UserApi userApi, DocumentResidencyForm documentResidencyForm) {
        documentResidencyForm.setTypeDocumentResidency(
                categoriesMapper.mapSubCategory(documentResidencyForm.getTypeDocumentResidency(), userApi));
    }

    @PostMapping(value = "/account", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> account(@Validated(ApiPartner.class) @RequestBody AccountPartnerForm accountForm) {
        var tenantModel = tenantService.saveStepRegister(null, accountForm, StepRegister.ACCOUNT_PARTNER_API);
        logService.saveLog(LogType.ACCOUNT_CREATED, tenantModel.getId());
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#namesForm.tenantId)")
    @PostMapping(value = "/names", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> names(@Validated(ApiPartner.class) @RequestBody NamesForm namesForm) {
        var tenant = tenantService.findById(namesForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, namesForm, StepRegister.NAMES);
        logService.saveStepLog(tenantModel.getId(), StepRegister.NAMES.getClazz().getSimpleName());
        return ok(tenantModel);
    }

    @ApiOperation("En cas de couple(COUPLE), le nom et le prénom sont requis mais pas l'email.<br/>En cas de colocation(GROUP): nom, prénom et email sont requis")
    @PreAuthorize("hasPermissionOnTenant(#applicationForm.tenantId)")
    @PostMapping(value = "/application/v2", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> application(@Validated(ApiPartner.class) @RequestBody ApplicationFormV2 applicationForm) {
        var tenant = tenantService.findById(applicationForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, applicationForm, StepRegister.APPLICATION);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#honorDeclarationForm.tenantId)")
    @PostMapping(value = "/honorDeclaration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> honorDeclaration(@Validated(ApiPartner.class) @RequestBody HonorDeclarationForm honorDeclarationForm) {
        log.info("Tenant Id:" + honorDeclarationForm.getTenantId());
        var tenant = tenantService.findById(honorDeclarationForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, honorDeclarationForm, StepRegister.HONOR_DECLARATION);
        logService.saveLog(LogType.ACCOUNT_COMPLETED, tenantModel.getId());
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentIdentificationForm.tenantId)")
    @PostMapping(value = "/documentIdentification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TenantModel> documentIdentification(@Validated(ApiPartner.class) DocumentIdentificationForm documentIdentificationForm) {
        var tenant = tenantService.findById(documentIdentificationForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentIdentificationForm, StepRegister.DOCUMENT_IDENTIFICATION);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentResidencyForm.tenantId)")
    @PostMapping(value = "/documentResidency", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> documentResidency(DocumentResidencyForm documentResidencyForm) {
        // Validate form according partner api version
        UserApi userApi = clientAuthenticationFacade.getClient();
        validate(documentResidencyForm, ApiPartner.class, ApiVersion.getVersionClass(userApi.getVersion()));
        convert(userApi, documentResidencyForm);

        var tenant = tenantService.findById(documentResidencyForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentResidencyForm, StepRegister.DOCUMENT_RESIDENCY);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentProfessionalForm.tenantId)")
    @PostMapping(value = "/documentProfessional", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TenantModel> documentProfessional(@Validated(ApiPartner.class) DocumentProfessionalForm documentProfessionalForm) {
        var tenant = tenantService.findById(documentProfessionalForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentProfessionalForm, StepRegister.DOCUMENT_PROFESSIONAL);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentFinancialForm.tenantId)")
    @PostMapping(value = "/documentFinancial", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Register a financial document", notes = "Save a financial document for a tenant, a categoryStep can be added but is not mandatory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document has been saved", response = TenantModel.class),
            @ApiResponse(code = 403, message = "Forbidden: User not verified"),
            @ApiResponse(code = 400, message = "Wrong request params")
    })
    public ResponseEntity<TenantModel> documentFinancial(@Validated(ApiPartner.class) DocumentFinancialForm documentFinancialForm) {
        financialDocumentService.setFinancialDocumentCategoryStep(documentFinancialForm);
        var tenant = tenantService.findById(documentFinancialForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentFinancialForm, StepRegister.DOCUMENT_FINANCIAL);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#documentTaxForm.tenantId)")
    @PostMapping(value = "/documentTax", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TenantModel> documentTax(@Validated(ApiPartner.class) DocumentTaxForm documentTaxForm) {
        var tenant = tenantService.findById(documentTaxForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, documentTaxForm, StepRegister.DOCUMENT_TAX);
        return ok(tenantModel);
    }

    @PreAuthorize("hasPermissionOnTenant(#guarantorTypeForm.tenantId)")
    @PostMapping(value = "/guarantorType", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> guarantor(@Validated(ApiPartner.class) @RequestBody GuarantorTypeForm guarantorTypeForm) {
        var tenant = tenantService.findById(guarantorTypeForm.getTenantId());
        var tenantModel = tenantService.saveStepRegister(tenant, guarantorTypeForm, StepRegister.GUARANTOR_TYPE);
        logService.saveLog(LogType.ACCOUNT_EDITED, tenantModel.getId());
        return ok(tenantModel);
    }
}
