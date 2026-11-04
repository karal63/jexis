import { ArrowLeft, Globe2, Leaf, SunMoon } from "lucide-react";
import { Link } from "react-router-dom";
import { useI18n } from "@/shared/i18n/useI18n";

export default function SettingsPage() {
    const { t } = useI18n();

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

            <section className="mt-[clamp(52px,10vh,112px)] w-full max-w-[460px]">
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

                <div className="mt-8 divide-y divide-border rounded-lg border border-border">
                    <div className="flex items-center gap-4 px-4 py-5">
                        <SunMoon aria-hidden="true" size={18} className="text-brand-leaf" />
                        <span className="flex-1 text-sm font-semibold">{t("home.appearance")}</span>
                        <span className="text-xs text-muted-foreground">
                            {t("settings.comingSoon")}
                        </span>
                    </div>
                    <div className="flex items-center gap-4 px-4 py-5">
                        <Globe2 aria-hidden="true" size={18} className="text-brand-leaf" />
                        <span className="flex-1 text-sm font-semibold">{t("language.label")}</span>
                        <span className="text-xs text-muted-foreground">
                            {t("settings.comingSoon")}
                        </span>
                    </div>
                </div>
            </section>
        </main>
    );
}
