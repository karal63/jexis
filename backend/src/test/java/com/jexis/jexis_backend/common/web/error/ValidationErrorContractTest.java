package com.jexis.jexis_backend.common.web.error;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationErrorContractTest {
    @ParameterizedTest
    @CsvSource({
            "NotBlank, VALIDATION_NOT_BLANK",
            "NotNull, VALIDATION_NOT_NULL",
            "NotEmpty, VALIDATION_NOT_EMPTY",
            "Email, VALIDATION_EMAIL",
            "Size, VALIDATION_SIZE"
    })
    void returnsConsistentPublicValidationCodes(String constraint, String expected) {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "firstName", null, false,
                new String[]{constraint}, null, "Invalid field"));
        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(binding);

        var response = new GlobalExceptionHandler().handleValidation(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getErrors()).singleElement().satisfies(error -> {
            assertThat(error.field()).isEqualTo("firstName");
            assertThat(error.code()).isEqualTo(expected);
        });
    }
}
