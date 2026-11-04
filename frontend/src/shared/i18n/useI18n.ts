import "./index";
import { useTranslation } from "react-i18next";
import type { Locale, Translator } from "./en";

export function useI18n(): { t: Translator; locale: Locale; setLocale: (locale: Locale) => void } {
    const { t, i18n } = useTranslation();
    return {
        t: (key, values) => t(key, values),
        locale: i18n.language === "pl" ? "pl" : "en",
        setLocale: (locale) => {
            void i18n.changeLanguage(locale);
        },
    };
}
