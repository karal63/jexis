package com.jexis.jexis_backend.common.validation;

import com.jexis.jexis_backend.auth.application.dto.SignupDto;
import com.jexis.jexis_backend.user.application.dto.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {
    @Test void everyPasswordSettingRequestUsesTheSamePolicy() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (Class<?> type : List.of(SignupDto.class, CreateDto.class, AdminCreateDto.class,
                    ChangePasswordDto.class, ConfirmPasswordResetDto.class)) {
                for (String invalid : Arrays.asList(null, "", "        ", "1234567", "x".repeat(129))) {
                    assertThat(validator.validateValue(type, "password", invalid))
                            .as("%s rejects invalid password", type.getSimpleName()).isNotEmpty();
                }
                for (String valid : List.of("12345678", "x".repeat(128))) {
                    assertThat(validator.validateValue(type, "password", valid))
                            .as("%s accepts policy boundaries", type.getSimpleName()).isEmpty();
                }
            }
        }
    }
}
