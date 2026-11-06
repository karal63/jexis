import { CircleDollarSign, Globe2, SunMoon } from "lucide-react";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { setTheme, type Theme } from "@/features/theme/store/themeSlice";
import { useI18n } from "@/shared/i18n/useI18n";
import { LanguageSelect } from "@/shared/ui/language-select";
const DEFAULT_CURRENCY = "USD";
export function AppearanceSettings() {
    const { t } = useI18n();
    const theme = useAppSelector((state) => state.theme.theme);
    const dispatch = useAppDispatch();
    return (
        <div className="space-y-10">
            <section aria-labelledby="preferences-heading">
                <h2 id="preferences-heading" className="font-display text-lg font-bold">
                    {t("settings.preferences")}
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("settings.preferencesDescription")}
                </p>
                <div className="mt-4 divide-y divide-border rounded-lg border border-border">
                    <div className="flex flex-col gap-4 px-5 py-5 sm:flex-row sm:items-center">
                        <div className="flex min-w-0 flex-1 items-start gap-4">
                            <SunMoon
                                aria-hidden="true"
                                size={18}
                                className="shrink-0 text-brand-leaf"
                            />
                            <div className="min-w-0">
                                <label htmlFor="theme-select" className="text-sm font-semibold">
                                    {t("home.appearance")}
                                </label>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {t("settings.appearanceDescription")}
                                </p>
                            </div>
                        </div>
                        <select
                            id="theme-select"
                            value={theme}
                            onChange={(event) => dispatch(setTheme(event.target.value as Theme))}
                            className="min-h-10 w-full rounded-md border border-border bg-card px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring sm:w-40"
                        >
                            <option value="system">{t("home.system")}</option>
                            <option value="light">{t("home.light")}</option>
                            <option value="dark">{t("home.dark")}</option>
                        </select>
                    </div>
                    <div className="flex flex-col gap-4 px-5 py-5 sm:flex-row sm:items-center">
                        <div className="flex min-w-0 flex-1 items-start gap-4">
                            <Globe2
                                aria-hidden="true"
                                size={18}
                                className="shrink-0 text-brand-leaf"
                            />
                            <div className="min-w-0">
                                <label htmlFor="language-select" className="text-sm font-semibold">
                                    {t("language.label")}
                                </label>
                                <p className="mt-1 text-xs text-muted-foreground">
                                    {t("settings.languageDescription")}
                                </p>
                            </div>
                        </div>
                        <LanguageSelect
                            id="language-select"
                            hideLabel
                            className="min-h-10 w-full rounded-md border border-border bg-card px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring sm:w-40"
                        />
                    </div>
                </div>
            </section>

            <section aria-labelledby="currency-heading">
                <h2 id="currency-heading" className="font-display text-lg font-bold">
                    {t("settings.currencySection")}
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("settings.currencySectionDescription")}
                </p>
                <div className="mt-4 flex items-center gap-4 rounded-lg border border-border px-5 py-5">
                    <CircleDollarSign
                        aria-hidden="true"
                        size={18}
                        className="shrink-0 text-brand-leaf"
                    />
                    <div className="min-w-0 flex-1">
                        <h3 className="text-sm font-semibold">{t("settings.defaultCurrency")}</h3>
                        <p className="mt-1 text-xs text-muted-foreground">
                            {t("settings.currencyDescription")}
                        </p>
                    </div>
                    <span className="shrink-0 rounded-md border border-border bg-secondary px-3 py-1.5 text-sm font-semibold">
                        {DEFAULT_CURRENCY}
                    </span>
                </div>
            </section>
        </div>
    );
}
