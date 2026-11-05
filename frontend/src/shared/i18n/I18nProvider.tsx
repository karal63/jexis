import { useEffect, type ReactNode } from "react";
import { I18nextProvider } from "react-i18next";
import { useI18n } from "./useI18n";
import i18n from "./index";

function LocaleMetadata() {
    const { locale, t } = useI18n();
    useEffect(() => {
        document.documentElement.lang = locale;
        document.title = t("meta.title");
        document
            .querySelector('meta[name="description"]')
            ?.setAttribute("content", t("landing.intro"));
    }, [locale, t]);
    return null;
}

export function I18nProvider({ children }: { children: ReactNode }) {
    return (
        <I18nextProvider i18n={i18n}>
            <LocaleMetadata />
            {children}
        </I18nextProvider>
    );
}
