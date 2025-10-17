package com.niket.productreviewsystem.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoUrlValidator implements ConstraintValidator<NoUrl, String> {

    // Regex to detect common URL patterns (http://, https://, www., or .com, .org, etc.)
    private static final String URL_REGEX = "((http|https)://|www\\.|\\.[a-z]{2,3}/?)";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Null or empty comments are allowed
        }

        Matcher matcher = URL_PATTERN.matcher(value);
        return !matcher.find(); // Returns true if NO match (no URL) is found
    }
}