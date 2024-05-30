package fr.dossierfacile.api.front.validator.tenant.application;


import fr.dossierfacile.api.front.register.form.tenant.ApplicationFormV2;
import fr.dossierfacile.api.front.validator.annotation.tenant.application.v2.CheckTenantTypeAcceptAccess;
import fr.dossierfacile.common.enums.ApplicationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class CheckTenantTypeAcceptAccessValidator implements ConstraintValidator<CheckTenantTypeAcceptAccess, ApplicationFormV2> {
    @Override
    public void initialize(CheckTenantTypeAcceptAccess constraintAnnotation) {
        //this method is empty
    }

    @Override
    public boolean isValid(ApplicationFormV2 applicationForm, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = (applicationForm.getApplicationType() == ApplicationType.ALONE)
                || ((applicationForm.getApplicationType() == ApplicationType.GROUP
                || applicationForm.getApplicationType() == ApplicationType.COUPLE)
                && applicationForm.getAcceptAccess() != null
                && applicationForm.getAcceptAccess());

        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("{jakarta.validation.constraints.AssertTrue.message}")
                    .addPropertyNode("acceptAccess").addConstraintViolation();
        }

        return isValid;
    }
}
