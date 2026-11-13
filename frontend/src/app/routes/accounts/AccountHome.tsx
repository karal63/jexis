import { useEffect } from "react";
import { Navigate } from "react-router-dom";
import { useAppSelector } from "@/app/hooks";
import { useAccountQuery, useMyAccountsQuery } from "@/features/accounts";
import {
    clearLastVisitedAccount,
    getLastVisitedAccount,
} from "@/features/accounts/model/lastVisitedAccount";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

export default function AccountHome() {
    const { t } = useI18n();
    const userId = useAppSelector((state) => state.session.user?.id) ?? "";
    const lastAccountId = userId ? getLastVisitedAccount(userId) : null;
    const savedAccount = useAccountQuery(lastAccountId ?? "", {
        skip: !lastAccountId,
        refetchOnMountOrArgChange: true,
    });
    const accounts = useMyAccountsQuery(
        { page: 1, search: "" },
        { skip: Boolean(lastAccountId), refetchOnMountOrArgChange: true },
    );

    useEffect(() => {
        const status =
            savedAccount.error && "status" in savedAccount.error ? savedAccount.error.status : null;
        if (lastAccountId && (status === 403 || status === 404) && !savedAccount.isFetching) {
            clearLastVisitedAccount(userId, lastAccountId);
        }
    }, [userId, lastAccountId, savedAccount.error, savedAccount.isFetching]);

    if (lastAccountId) {
        if (savedAccount.isFetching) return <PageLoader label={t("accounts.loadingAccount")} />;
        if (savedAccount.error) {
            return <Navigate to="/app/accounts" replace />;
        }
        if (savedAccount.data) {
            return <Navigate to={`/app/accounts/${lastAccountId}`} replace />;
        }
        return <PageLoader label={t("accounts.loadingAccount")} />;
    }

    if (accounts.isFetching) return <PageLoader label={t("accounts.loading")} />;
    if (accounts.data) {
        const onlyAccount = accounts.data.totalItems === 1 && accounts.data.items[0];
        return (
            <Navigate
                to={onlyAccount ? `/app/accounts/${onlyAccount.id}` : "/app/accounts"}
                replace
            />
        );
    }
    if (accounts.error) return <Navigate to="/app/accounts" replace />;
    return <PageLoader label={t("accounts.loading")} />;
}
