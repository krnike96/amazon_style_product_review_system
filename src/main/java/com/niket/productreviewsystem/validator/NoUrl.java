package com.niket.productreviewsystem.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoUrlValidator.class)
public @interface NoUrl {
    String message() default "Review comment must not contain URLs or links.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}