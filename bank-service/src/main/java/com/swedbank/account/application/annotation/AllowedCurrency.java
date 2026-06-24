package com.swedbank.account.application.annotation;

import com.swedbank.account.application.validation.CurrencyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedCurrency {
    String message() default "Currency is not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
