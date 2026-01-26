package com.health.spry.validators;

import java.time.Year;

import com.health.spry.annotations.YearConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;



public class PastOrPresentYearValidator implements ConstraintValidator<YearConstraint, Integer> {
    
    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // Use @NotNull separately if needed
        }
        return year <= Year.now().getValue();
    }
}
