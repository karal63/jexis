import { ArrowLeft, Building2 } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { accountName, useAccountQuery } from "@/features/accounts";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { useI18n } from "@/shared/i18n/useI18n";

export default function AccountPage() {
    const { accountId = "" } = useParams();
    const { t } = useI18n();
    const {
        data: account,
        error,
        isLoading,
        refetch,
    } = useAccountQuery(accountId, { skip: !accountId });

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
                {isLoading ? (
                    <p role="status" className="mt-12 text-muted-foreground">
                        {t("accounts.loadingAccount")}
                    </p>
                ) : error ? (
                    <div className="mt-12 rounded-lg border border-danger-line bg-danger-bg p-8">
                        <p role="alert" className="text-danger">
                            {getApiError(error, t).message}
                        </p>
                        <Button className="mt-4" onClick={() => refetch()}>
                            {t("accounts.retry")}
                        </Button>
                    </div>
                ) : account ? (
                    <section className="mt-12 rounded-lg border border-border bg-card p-8">
                        <span className="grid size-12 place-items-center rounded-lg bg-accent text-brand-leaf">
                            <Building2 size={23} />
                        </span>
                        <p className="mt-7 text-xs font-bold uppercase tracking-[.14em] text-brand-leaf">
                            {t("accounts.accountOverview")}
                        </p>
                        <h1 className="mt-2 font-display text-3xl font-bold">
                            {accountName(account) || t("accounts.untitled")}
                        </h1>
                        {account.email && (
                            <p className="mt-2 text-sm text-muted-foreground">{account.email}</p>
                        )}
                    </section>
                ) : null}
            </div>
        </main>
    );
}
