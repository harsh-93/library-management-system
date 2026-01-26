package com.health.spry.validators;

import java.util.Arrays;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import com.health.spry.annotations.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;



public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {private int minLength;
private int maxLength;

@Override
public void initialize(ValidPassword constraintAnnotation) {
    this.minLength = constraintAnnotation.minLength();
    this.maxLength = constraintAnnotation.maxLength();
}

@Override
public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
        return false;
    }
    
    PasswordValidator validator = new PasswordValidator(Arrays.asList(
        // Length rule
        new LengthRule(minLength, maxLength),
        
        // At least one uppercase character
        new CharacterRule(EnglishCharacterData.UpperCase, 1),
        
        // At least one digit
        new CharacterRule(EnglishCharacterData.Digit, 1),
        
        // At least one special character
        new CharacterRule(EnglishCharacterData.Special, 1),
        
        // No whitespace allowed
        new WhitespaceRule()
    ));
    
    RuleResult result = validator.validate(new PasswordData(password));
    
    if (result.isValid()) {
        return true;
    }
    
    // Build custom error message
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(
        String.join(", ", validator.getMessages(result))
    ).addConstraintViolation();
    
    return false;
}}