package io.github.raphaelmun1z.ChatGepeteco.dtos.security;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSignUpRequest {
    String message() default "Dados de cadastro inválidos para o tipo de usuário selecionado.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}