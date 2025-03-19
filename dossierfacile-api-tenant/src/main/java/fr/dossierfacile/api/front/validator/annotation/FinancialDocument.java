package fr.dossierfacile.api.front.validator.annotation;

import fr.dossierfacile.api.front.validator.FinancialDocumentCategoriesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {FinancialDocumentCategoriesValidator.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinancialDocument {
    String message() default "Financial document is not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
