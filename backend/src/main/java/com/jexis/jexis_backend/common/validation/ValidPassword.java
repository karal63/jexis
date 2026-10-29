package com.jexis.jexis_backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;

/** Shared policy for setting a new password. */
@Documented
@Constraint(validatedBy = {})
@NotBlank(message = "Password cannot be blank")
@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
