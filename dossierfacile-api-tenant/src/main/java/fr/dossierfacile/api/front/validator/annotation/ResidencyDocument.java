package fr.dossierfacile.api.front.validator.annotation;

import fr.dossierfacile.api.front.validator.ResidencyDocumentCategoriesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {ResidencyDocumentCategoriesValidator.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResidencyDocument {
    String message() default "Residency document is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean isGuarantorMode() default false;
}
