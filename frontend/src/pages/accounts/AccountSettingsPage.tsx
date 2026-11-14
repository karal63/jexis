import { loadConnectAndInitialize } from "@stripe/connect-js/pure";
import { ConnectAccountManagement, ConnectComponentsProvider } from "@stripe/react-connect-js";
import { ArrowLeft } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { useAccountOnboardingQuery, useBankAccountSessionMutation } from "@/features/accounts";
import { AccountDeletionSection } from "@/features/accounts/ui/AccountDeletionSection";
import { getApiError } from "@/shared/api/apiError";
import { useI18n } from "@/shared/i18n/useI18n";
import { Button } from "@/shared/ui/button";

export default function AccountSettingsPage() {
    const { accountId = "" } = useParams();
    const { t } = useI18n();
    const onboarding = useAccountOnboardingQuery(accountId, { skip: !accountId });
    const status = onboarding.currentData;
    const accountPath = `/app/accounts/${accountId}`;

    if (onboarding.isFetching) {
        return (
            <p role="status" className="text-sm text-muted-foreground">
                {t("accounts.loadingAccount")}
            </p>
        );
    }
    if (
        !onboarding.error &&
        (status?.status !== "COMPLETED" || (status.canOnboard && status.externalAccountRequired))
    ) {
        return <Navigate to={accountPath} replace />;
    }

    return (
        <section aria-labelledby="account-settings-title" className="w-full min-w-0">
            <header className="border-b pb-6">
                <Link
                    to={accountPath}
                    className="inline-flex items-center gap-2 text-xs font-semibold text-brand-leaf hover:underline"
                >
                    <ArrowLeft aria-hidden="true" size={14} />
                    {t("accounts.accountOverview")}
                </Link>
                <h1
                    id="account-settings-title"
                    className="mt-5 font-display text-3xl font-bold tracking-tight"
                >
                    {t("settings.title")}
                </h1>
                <p className="mt-2 text-sm text-muted-foreground">{t("accounts.settingsIntro")}</p>
            </header>
            <div className="min-w-0 pt-6">
                {onboarding.error ? (
                    <>
                        <p role="alert" className="text-sm text-danger">
                            {getApiError(onboarding.error, t).message}
                        </p>
                        <Button className="mt-4" onClick={() => void onboarding.refetch()}>
                            {t("accounts.retry")}
                        </Button>
                    </>
                ) : status?.canOnboard ? (
                    <StripeAccountSettings key={accountId} accountId={accountId} />
                ) : (
                    <p className="text-sm text-muted-foreground">
                        {t("accounts.settingsContactAdmin")}
                    </p>
                )}
            </div>

            {!onboarding.error && status?.canDelete && (
                <AccountDeletionSection key={accountId} accountId={accountId} />
            )}
        </section>
    );
}

function StripeAccountSettings({ accountId }: { accountId: string }) {
    const { t, locale } = useI18n();
    const [createBankAccountSession] = useBankAccountSessionMutation();
    const [instance, setInstance] = useState<ReturnType<typeof loadConnectAndInitialize>>();
    const [failed, setFailed] = useState(false);
    const initialized = useRef(false);
    const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY as string | undefined;

    const loadSettings = useCallback(() => {
        if (!publishableKey) return;
        setFailed(false);
        setInstance(
            loadConnectAndInitialize({
                publishableKey,
                locale: locale === "pl" ? "pl-PL" : "en-US",
                appearance: stripeAppearance(),
                fetchClientSecret: async () => {
                    const session = await createBankAccountSession({
                        id: accountId,
                        manage: true,
                    }).unwrap();
                    return session.clientSecret;
                },
            }),
        );
    }, [accountId, createBankAccountSession, locale, publishableKey]);

    useEffect(() => {
        // Strict Mode replays this effect, and each initialization requests a Stripe session.
        if (initialized.current) return;
        initialized.current = true;
        loadSettings();
    }, [loadSettings]);

    useEffect(() => {
        instance?.update({ locale: locale === "pl" ? "pl-PL" : "en-US" });
    }, [instance, locale]);

    useEffect(() => {
        if (!instance) return;
        const updateAppearance = () => instance.update({ appearance: stripeAppearance() });
        const observer = new MutationObserver(updateAppearance);
        observer.observe(document.documentElement, {
            attributes: true,
            attributeFilter: ["data-theme"],
        });
        updateAppearance();
        return () => observer.disconnect();
    }, [instance]);

    if (!publishableKey) return <p role="status">{t("accounts.settingsUnavailable")}</p>;
    if (failed) {
        return (
            <>
                <p role="alert" className="text-sm text-danger">
                    {t("accounts.settingsLoadError")}
                </p>
                <Button className="mt-4" onClick={loadSettings}>
                    {t("accounts.retry")}
                </Button>
            </>
        );
    }
    if (!instance) return null;

    return (
        <ConnectComponentsProvider connectInstance={instance}>
            <ConnectAccountManagement onLoadError={() => setFailed(true)} />
        </ConnectComponentsProvider>
    );
}

function stripeAppearance() {
    const styles = getComputedStyle(document.documentElement);
    const color = (variable: string) => styles.getPropertyValue(variable).trim();
    return {
        variables: {
            colorPrimary: color("--brand-green"),
            colorBackground: color("--background"),
            colorText: color("--foreground"),
            colorSecondaryText: color("--muted-foreground"),
            colorBorder: color("--border"),
            formHighlightColorBorder: color("--ring"),
            formBackgroundColor: color("--card"),
            offsetBackgroundColor: color("--accent"),
        },
    };
}
