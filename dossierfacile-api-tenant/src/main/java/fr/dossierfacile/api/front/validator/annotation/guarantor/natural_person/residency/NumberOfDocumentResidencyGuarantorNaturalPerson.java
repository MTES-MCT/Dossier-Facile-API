package fr.dossierfacile.api.front.validator.annotation.guarantor.natural_person.residency;

import fr.dossierfacile.api.front.validator.guarantor.natural_person.residency.NumberOfDocumentResidencyGuarantorNaturalPersonValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

@Constraint(
        validatedBy = {NumberOfDocumentResidencyGuarantorNaturalPersonValidator.class}
)

public @interface NumberOfDocumentResidencyGuarantorNaturalPerson {
    String message() default "number of document must be between 1 and 10 and not exceed 50Mb in total";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
