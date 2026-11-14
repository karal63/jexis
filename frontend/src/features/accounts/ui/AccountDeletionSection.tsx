import { Trash2 } from "lucide-react";
import { useId, useState } from "react";
import { useNavigate } from "react-router-dom";
import { accountName, useAccountQuery } from "@/features/accounts";
import { useDeleteAccountMutation } from "@/features/accounts/api/accountsApi";
import { clearLastVisitedAccount } from "@/features/accounts/model/lastVisitedAccount";
import { useAppSelector } from "@/app/hooks";
import { getApiError } from "@/shared/api/apiError";
import { useI18n } from "@/shared/i18n/useI18n";
import { Button } from "@/shared/ui/button";
import { Dialog } from "@/shared/ui/dialog";

export function AccountDeletionSection({
    accountId,
    disabled = false,
}: {
    accountId: string;
    disabled?: boolean;
}) {
    const titleId = useId();
    const { t } = useI18n();
    const navigate = useNavigate();
    const userId = useAppSelector((state) => state.session.user?.id);
    const { currentData: account } = useAccountQuery(accountId);
    const [confirming, setConfirming] = useState(false);
    const [deleteAccount, { isLoading, error, reset }] = useDeleteAccountMutation();
    const name = (account && accountName(account)) || t("accounts.untitled");

    function cancel() {
        if (isLoading) return;
        setConfirming(false);
        reset();
    }

    async function confirm() {
        if (isLoading) return;
        try {
            await deleteAccount(accountId).unwrap();
            if (userId) clearLastVisitedAccount(userId, accountId);
            navigate("/app/accounts", { replace: true });
        } catch {
            // The mutation exposes the error so the user can retry or cancel.
        }
    }

    return (
        <section
            aria-labelledby={titleId}
            className="mt-8 flex flex-wrap items-center justify-between gap-4 rounded-lg border border-transparent bg-transparent py-4 sm:py-5"
        >
            <div className="min-w-0 flex-1 basis-44">
                <h2 id={titleId} className="font-display text-base font-bold text-danger">
                    {t("accounts.deleteTitle")}
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("accounts.deleteDescription")}
                </p>
            </div>
            <Button
                variant="secondary"
                className="border-danger bg-card text-danger hover:bg-card"
                aria-haspopup="dialog"
                disabled={disabled}
                onClick={() => setConfirming(true)}
            >
                <Trash2 aria-hidden="true" size={16} />
                {t("accounts.delete")}
            </Button>
            {confirming && (
                <Dialog
                    title={t("accounts.deleteConfirmTitle")}
                    description={t("accounts.deleteConfirmDescription", { name })}
                    onClose={cancel}
                    alert
                >
                    {isLoading && (
                        <p role="status" className="sr-only">
                            {t("accounts.deleting")}
                        </p>
                    )}
                    {error && (
                        <p role="alert" className="mt-4 text-sm text-danger">
                            {getApiError(error, t).message}
                        </p>
                    )}
                    <div className="mt-6 flex flex-wrap justify-end gap-3">
                        <Button variant="secondary" onClick={cancel} disabled={isLoading}>
                            {t("accounts.deleteCancel")}
                        </Button>
                        <Button
                            className="bg-destructive text-card hover:bg-destructive/85"
                            onClick={() => void confirm()}
                            disabled={isLoading}
                            aria-busy={isLoading}
                        >
                            {t(isLoading ? "accounts.deleting" : "accounts.delete")}
                        </Button>
                    </div>
                </Dialog>
            )}
        </section>
    );
}
