package com.jexis.jexis_backend.user.domain;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

/** Validation and canonical storage values shared by user creation, editing, and the phone migration. */
public final class ProfileInputNormalizer {
    private ProfileInputNormalizer() {}

    public static String name(String raw, String field) {
        if (raw == null || raw.strip().isEmpty()) {
            throw new ProfileValidationException(field, "VALIDATION_NOT_BLANK");
        }
        String value = raw.strip();
        if (value.length() > 100) {
            throw new ProfileValidationException(field, "VALIDATION_SIZE");
        }
        return value;
    }

    public static String phone(String raw) {
        if (raw == null) return null;
        if (raw.length() > 64) throw invalidPhone();
        String value = raw.strip();
        if (value.isEmpty()) return null;
        if (!value.matches("^\\+[0-9()\\s-]+$")) throw invalidPhone();
        try {
            var util = PhoneNumberUtil.getInstance();
            var parsed = util.parse(value, null);
            if (!util.isValidNumber(parsed) || parsed.hasExtension()) throw invalidPhone();
            return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException ex) {
            throw invalidPhone();
        }
    }

    private static ProfileValidationException invalidPhone() {
        return new ProfileValidationException("phoneNumber", "VALIDATION_PHONE");
    }
}
