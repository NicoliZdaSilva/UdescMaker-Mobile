package com.udescmaker.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YoutubeUrlValidator.class)
public @interface YoutubeUrl {
    String message() default "deve ser uma URL válida do YouTube";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
