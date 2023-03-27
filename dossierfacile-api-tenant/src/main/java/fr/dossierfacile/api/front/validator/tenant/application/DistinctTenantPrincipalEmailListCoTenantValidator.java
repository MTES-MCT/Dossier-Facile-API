package fr.dossierfacile.api.front.validator.tenant.application;

import fr.dossierfacile.api.front.register.form.tenant.ApplicationForm;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.anotation.tenant.application.DistinctTenantPrincipalEmailListCoTenant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * deprecated  since 202209
 */
@Deprecated
@Component
@AllArgsConstructor
public class DistinctTenantPrincipalEmailListCoTenantValidator implements ConstraintValidator<DistinctTenantPrincipalEmailListCoTenant, ApplicationForm> {

    private final TenantService tenantService;

    @Override
    public void initialize(DistinctTenantPrincipalEmailListCoTenant constraintAnnotation) {
        //this method is empty
    }

    @Override
    public boolean isValid(ApplicationForm applicationForm, ConstraintValidatorContext constraintValidatorContext) {
        var tenant = tenantService.findById(applicationForm.getTenantId());
        if (tenant == null) {
            return true;
        }
        var isValid = !applicationForm.getCoTenantEmail().contains(tenant.getEmail());
        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("coTenantEmail").addConstraintViolation();
        }
        return isValid;
    }
}
