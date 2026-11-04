package com.jexis.jexis_backend.emailService.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

class EmailMessagesTest {
    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void sendsPolishEmailCopyWhenRequestIsPolish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("pl"));
        assertEquals("Aktywuj swoje konto", EmailMessages.activationSubject());
        assertTrue(EmailMessages.activationBody("Ala", "https://example.com/activate").contains("Kliknij poniższy link"));
        assertEquals("Zresetuj hasło", EmailMessages.resetSubject());
        assertTrue(EmailMessages.resetBody("Ala", "https://example.com/reset").contains("30 minut"));
    }

    @Test
    void defaultsToEnglishForOtherLanguages() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("de"));
        assertEquals("Activate your account", EmailMessages.activationSubject());
        assertEquals("Reset your password", EmailMessages.resetSubject());
    }
}
