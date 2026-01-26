package br.edu.ifpb.pweb2.colegiplus.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatriculaValidator.class)
public @interface MatriculaValida {
    String message() default "Matrícula inválida! Deve ter 11 dígitos e começar com 20.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}