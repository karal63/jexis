import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import { en } from "./en";
import { pl } from "./pl";

function initialLocale(): "en" | "pl" {
    try {
        const stored = localStorage.getItem("locale");
        if (stored === "en" || stored === "pl") return stored;
    } catch {
        // Storage may be unavailable.
    }
    return typeof navigator !== "undefined" && navigator.language.toLowerCase().startsWith("pl")
        ? "pl"
        : "en";
}

void i18n.use(initReactI18next).init({
    resources: { en: { translation: en }, pl: { translation: pl } },
    lng: initialLocale(),
    fallbackLng: "en",
    supportedLngs: ["en", "pl"],
    keySeparator: false,
    initAsync: false,
    interpolation: { escapeValue: false },
});

i18n.on("languageChanged", (locale) => {
    try {
        localStorage.setItem("locale", locale);
    } catch {
        // Storage may be unavailable.
    }
});

export default i18n;
