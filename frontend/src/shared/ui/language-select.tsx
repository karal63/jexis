import { useI18n } from "@/shared/i18n/useI18n";
import type { Locale } from "@/shared/i18n/en";

export function LanguageSelect() {
    const { locale, setLocale, t } = useI18n();
    return (
        <label className="inline-flex items-center gap-2 text-xs text-muted-foreground">
            <span>{t("language.label")}</span>
            <select
                aria-label={t("language.label")}
                className="rounded-md border border-border bg-card px-2 py-1 text-foreground"
                value={locale}
                onChange={(event) => setLocale(event.target.value as Locale)}
            >
                <option value="en">{t("language.en")}</option>
                <option value="pl">{t("language.pl")}</option>
            </select>
        </label>
    );
}
