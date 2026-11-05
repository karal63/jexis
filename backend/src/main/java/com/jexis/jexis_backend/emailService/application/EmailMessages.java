package com.jexis.jexis_backend.emailService.application;

import org.springframework.context.i18n.LocaleContextHolder;

/** User-facing email copy in the language selected by the requesting client. */
public final class EmailMessages {
    private EmailMessages() {}

    private static boolean polish() {
        return "pl".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    public static String activationSubject() {
        return polish() ? "Aktywuj swoje konto" : "Activate your account";
    }

    public static String activationBody(String name, String url) {
        return polish()
                ? """
                        Cześć, %s!

                        Kliknij poniższy link, aby aktywować konto:

                        %s
                        """.formatted(name, url)
                : """
                        Hi, %s

                        Please click the following link to activate your account:

                        %s
                        """.formatted(name, url);
    }

    public static String resetSubject() {
        return polish() ? "Zresetuj hasło" : "Reset your password";
    }

    public static String resetBody(String name, String url) {
        return polish()
                ? """
                        Cześć, %s!

                        Otrzymaliśmy prośbę o zresetowanie Twojego hasła.

                        Kliknij poniższy link, aby ustawić nowe hasło:

                        %s

                        Link wygaśnie za 30 minut.

                        Jeśli to nie Ty wysłałeś tę prośbę, zignoruj tę wiadomość.
                        """.formatted(name, url)
                : """
                        Hi %s,

                        We received a request to reset your password.

                        Click this link below to reset your password:

                        %s

                        This link expires in 30 minutes.

                        If you didn't request this, ignore this email.
                        """.formatted(name, url);
    }
}
