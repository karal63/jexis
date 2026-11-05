import { ArrowLeft, CircleDollarSign, Globe2, Leaf, SunMoon } from "lucide-react";
import { Link } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { setTheme, type Theme } from "@/features/theme/store/themeSlice";
import type { Locale } from "@/shared/i18n/en";
import { useI18n } from "@/shared/i18n/useI18n";

const DEFAULT_CURRENCY = "USD";

export default function SettingsPage() {
    const { t, locale, setLocale } = useI18n();
    const theme = useAppSelector((state) => state.theme.theme);
    const dispatch = useAppDispatch();

    return (
        <main className="flex min-h-dvh flex-col items-center bg-card px-5 pb-8 text-foreground">
            <Link
                to="/app"
                className="mt-[6vh] inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-.08em]"
                aria-label={t("accounts.allAccounts")}
            >
                <span className="grid size-7 place-items-center">
                    <Leaf size={18} strokeWidth={2.5} />
                </span>
                jexis
            </Link>

            <div className="mt-[clamp(52px,10vh,112px)] w-full max-w-[760px] pb-10">
                <Link
                    to="/app"
                    className="inline-flex items-center gap-2 text-xs font-semibold text-brand-leaf hover:underline"
                >
                    <ArrowLeft aria-hidden="true" size={14} />
                    {t("accounts.allAccounts")}
                </Link>
                <h1 className="mt-7 font-display text-[27px] font-bold tracking-[-.045em]">
                    {t("settings.title")}
                </h1>
                <p className="mt-2 text-sm text-muted-foreground">{t("settings.intro")}</p>

                <div className="mt-10 space-y-10">
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
                                        <label
                                            htmlFor="theme-select"
                                            className="text-sm font-semibold"
                                        >
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
                                    onChange={(event) =>
                                        dispatch(setTheme(event.target.value as Theme))
                                    }
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
                                        <label
                                            htmlFor="language-select"
                                            className="text-sm font-semibold"
                                        >
                                            {t("language.label")}
                                        </label>
                                        <p className="mt-1 text-xs text-muted-foreground">
                                            {t("settings.languageDescription")}
                                        </p>
                                    </div>
                                </div>
                                <select
                                    id="language-select"
                                    value={locale}
                                    onChange={(event) => setLocale(event.target.value as Locale)}
                                    className="min-h-10 w-full rounded-md border border-border bg-card px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring sm:w-40"
                                >
                                    <option value="en">{t("language.en")}</option>
                                    <option value="pl">{t("language.pl")}</option>
                                </select>
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
                                <h3 className="text-sm font-semibold">
                                    {t("settings.defaultCurrency")}
                                </h3>
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
            </div>
        </main>
    );
}
