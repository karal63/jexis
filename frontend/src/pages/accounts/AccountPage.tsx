import { useEffect } from "react";
import { ArrowLeft, Building2 } from "lucide-react";
import { Link, Navigate, useParams } from "react-router-dom";
import {
    accountName,
    useAccountQuery,
    useAccountOnboardingQuery,
    useCreateOnboardingLinkMutation,
} from "@/features/accounts";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { CenteredPage } from "@/shared/ui/centered-page";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

import ExternalAccountSetup from "./ExternalAccountSetup";

export default function AccountPage({ bankAccountMode }: { bankAccountMode?: "setup" | "manage" }) {
    const { accountId = "" } = useParams();
    return (
        <AccountOverview key={accountId} accountId={accountId} bankAccountMode={bankAccountMode} />
    );
}

function AccountOverview({
    accountId,
    bankAccountMode,
}: {
    accountId: string;
    bankAccountMode?: "setup" | "manage";
}) {
    const { t } = useI18n();
    const {
        data: account,
        error,
        isLoading,
        refetch,
    } = useAccountQuery(accountId, { skip: !accountId });

    const onboarding = useAccountOnboardingQuery(accountId, {
        skip: !accountId,
        refetchOnMountOrArgChange: true,
    });
    const [
        createLink,
        { data: link, error: linkError, isLoading: creatingLink, reset: resetLink },
    ] = useCreateOnboardingLinkMutation();
    const refreshStatus = onboarding.refetch;

    useEffect(() => {
        if (!link) return;
        if (link.url) {
            window.location.replace(link.url);
        } else {
            resetLink();
            void refreshStatus();
        }
    }, [link, refreshStatus, resetLink]);

    if (isLoading || onboarding.isFetching) {
        return <PageLoader label={t("accounts.loadingAccount")} />;
    }

    const status = onboarding.currentData?.status;
    const canOnboard = onboarding.currentData?.canOnboard;
    const notStarted = status === "NOT_STARTED";
    const accountError = error ?? onboarding.error;

    const name = account && (accountName(account) || t("accounts.untitled"));
    const onboardingDescription = t(
        canOnboard ? "accounts.onboardingRequired" : "accounts.onboardingContactAdmin",
    );
    const onboardingAction = canOnboard && (
        <>
            <Button
                className={notStarted ? "w-full" : "mt-4"}
                onClick={() => void createLink(accountId)}
                disabled={creatingLink || Boolean(link)}
                aria-busy={creatingLink}
            >
                {t(notStarted ? "accounts.onboardingStart" : "accounts.onboardingContinue")}
            </Button>
            {linkError && (
                <p role="alert" className="mt-4 text-sm text-danger">
                    {getApiError(linkError, t).message}
                </p>
            )}
        </>
    );

    const needsExternalAccount =
        status === "COMPLETED" && onboarding.currentData?.externalAccountRequired;
    const accountPath = `/app/accounts/${accountId}`;

    if (!accountError && account) {
        if (bankAccountMode) {
            const manage = bankAccountMode === "manage";
            return status === "COMPLETED" &&
                canOnboard &&
                (manage ? !needsExternalAccount : needsExternalAccount) ? (
                <ExternalAccountSetup
                    key={bankAccountMode}
                    accountId={accountId}
                    manage={manage}
                    onExit={() => void refreshStatus()}
                />
            ) : (
                <Navigate to={accountPath} replace />
            );
        }
        if (needsExternalAccount && canOnboard) {
            return <Navigate to={`${accountPath}/external-account`} replace />;
        }
    }

    if (!accountError && name && notStarted) {
        return (
            <CenteredPage
                title={name}
                description={onboardingDescription}
                backTo="/app"
                backLabel={t("accounts.allAccounts")}
            >
                {onboardingAction}
            </CenteredPage>
        );
    }

    return (
        <main className="min-h-screen bg-background px-5 py-10 text-foreground sm:px-8">
            <div className="mx-auto max-w-6xl">
                <Link
                    to="/app"
                    className="inline-flex items-center gap-2 text-sm font-semibold text-brand-leaf hover:underline"
                >
                    <ArrowLeft size={16} />
                    {t("accounts.allAccounts")}
                </Link>
                {accountError ? (
                    <div className="mt-12 rounded-lg border border-danger-line bg-danger-bg p-8">
                        <p role="alert" className="text-danger">
                            {getApiError(accountError, t).message}
                        </p>
                        <Button
                            className="mt-4"
                            onClick={() => {
                                void refetch();
                                void refreshStatus();
                            }}
                        >
                            {t("accounts.retry")}
                        </Button>
                    </div>
                ) : account ? (
                    <section className="mt-12 rounded-lg border border-border bg-card p-8">
                        <span className="grid size-12 place-items-center rounded-lg bg-accent text-brand-leaf">
                            <Building2 size={23} />
                        </span>
                        {status === "COMPLETED" && (
                            <p className="mt-7 text-xs font-bold uppercase tracking-[.14em] text-brand-leaf">
                                {t("accounts.accountOverview")}
                            </p>
                        )}
                        <h1 className="mt-2 font-display text-3xl font-bold">{name}</h1>
                        {account.email && (
                            <p className="mt-2 text-sm text-muted-foreground">{account.email}</p>
                        )}
                        {status === "COMPLETED" && canOnboard && (
                            <Button className="mt-4" to={`${accountPath}/bank-accounts`}>
                                {t("accounts.bankAccountsManage")}
                            </Button>
                        )}
                        {needsExternalAccount && !canOnboard && (
                            <p className="mt-4 text-sm text-muted-foreground">
                                {t("accounts.externalAccountContactAdmin")}
                            </p>
                        )}
                        {status === "ACTION_REQUIRED" && (
                            <>
                                <p className="mt-4 text-sm text-muted-foreground">
                                    {onboardingDescription}
                                </p>
                                {onboardingAction}
                            </>
                        )}
                        {status === "PENDING_VERIFICATION" && (
                            <>
                                <p role="status" className="mt-4 text-sm text-muted-foreground">
                                    {t("accounts.onboardingPending")}
                                </p>
                                <Button
                                    variant="secondary"
                                    className="mt-4"
                                    onClick={() => void refreshStatus()}
                                >
                                    {t("accounts.onboardingCheckStatus")}
                                </Button>
                            </>
                        )}
                        {status === "RESTRICTED" && (
                            <p role="status" className="mt-4 text-sm text-muted-foreground">
                                {t("accounts.onboardingRestricted")}
                            </p>
                        )}
                    </section>
                ) : null}
            </div>
        </main>
    );
}
