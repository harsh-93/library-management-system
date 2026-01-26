package com.health.spry.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.health.spry.validators.PastOrPresentYearValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastOrPresentYearValidator.class)
@Documented
public @interface YearConstraint {
    String message() default "Year must not be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}