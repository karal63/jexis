import { useEffect } from "react";
import { Navigate, useParams } from "react-router-dom";
import {
    accountName,
    useAccountQuery,
    useAccountOnboardingQuery,
    useCreateOnboardingLinkMutation,
} from "@/features/accounts";
import { AccountDeletionSection } from "@/features/accounts/ui/AccountDeletionSection";
import ExternalAccountSetup from "@/features/accounts/ui/ExternalAccountSetup";
import { setLastVisitedAccount } from "@/features/accounts/model/lastVisitedAccount";
import { useAppSelector } from "@/app/hooks";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { CenteredPage } from "@/shared/ui/centered-page";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

export default function AccountPage({ bankAccountSetup = false }: { bankAccountSetup?: boolean }) {
    const { accountId = "" } = useParams();
    return (
        <AccountOverview
            key={accountId}
            accountId={accountId}
            bankAccountSetup={bankAccountSetup}
        />
    );
}

function AccountOverview({
    accountId,
    bankAccountSetup,
}: {
    accountId: string;
    bankAccountSetup: boolean;
}) {
    const { t } = useI18n();
    const userId = useAppSelector((state) => state.session.user?.id);
    const {
        data: account,
        error,
        isLoading,
        refetch,
    } = useAccountQuery(accountId, { skip: !accountId });

    useEffect(() => {
        if (userId && account) setLastVisitedAccount(userId, account.id);
    }, [userId, account]);

    const onboarding = useAccountOnboardingQuery(accountId, {
        skip: !accountId,
        refetchOnMountOrArgChange: !bankAccountSetup,
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
    const canDelete = onboarding.currentData?.canDelete ?? false;
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
    const deletionSection = canDelete && (
        <AccountDeletionSection accountId={accountId} disabled={creatingLink || Boolean(link)} />
    );

    if (!accountError && account) {
        if (bankAccountSetup) {
            return needsExternalAccount && canOnboard ? (
                <ExternalAccountSetup
                    accountId={accountId}
                    canDelete={canDelete}
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

    if (!accountError && name && (notStarted || status === "ACTION_REQUIRED")) {
        return (
            <CenteredPage
                title={name}
                description={onboardingDescription}
                backTo="/app/accounts"
                backLabel={t("accounts.allAccounts")}
            >
                {onboardingAction}
                {deletionSection}
            </CenteredPage>
        );
    }

    if (!accountError && name && (status === "PENDING_VERIFICATION" || status === "RESTRICTED")) {
        return (
            <CenteredPage
                title={name}
                description={t(
                    status === "PENDING_VERIFICATION"
                        ? "accounts.onboardingPending"
                        : "accounts.onboardingRestricted",
                )}
                backTo="/app/accounts"
                backLabel={t("accounts.allAccounts")}
            >
                {status === "PENDING_VERIFICATION" && (
                    <Button variant="secondary" onClick={() => void refreshStatus()}>
                        {t("accounts.onboardingCheckStatus")}
                    </Button>
                )}
                {deletionSection}
            </CenteredPage>
        );
    }

    return (
        <>
            {accountError ? (
                <div className="rounded-lg border border-danger-line bg-danger-bg p-6">
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
                <section aria-labelledby="account-title">
                    <div className="border-b pb-6">
                        {status === "COMPLETED" && (
                            <p className="mb-2 text-sm text-muted-foreground">
                                {t("accounts.accountOverview")}
                            </p>
                        )}
                        <h1
                            id="account-title"
                            className="wrap-anywhere font-display text-3xl font-bold tracking-tight"
                        >
                            {name}
                        </h1>
                        {account.email && (
                            <p className="mt-2 wrap-anywhere text-sm text-muted-foreground">
                                {account.email}
                            </p>
                        )}
                    </div>
                    {needsExternalAccount && !canOnboard && (
                        <p className="mt-4 text-sm text-muted-foreground">
                            {t("accounts.externalAccountContactAdmin")}
                        </p>
                    )}
                </section>
            ) : null}
        </>
    );
}
