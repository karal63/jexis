import { loadStripe } from "@stripe/stripe-js/pure";
import { useState } from "react";
import {
    useCompletePayoutBankSetupMutation,
    usePayoutBankSetupMutation,
} from "@/features/accounts";
import { useI18n } from "@/shared/i18n/useI18n";
import { Button } from "@/shared/ui/button";
import { CenteredPage } from "@/shared/ui/centered-page";
import { AccountDeletionSection } from "./AccountDeletionSection";

export default function ExternalAccountSetup({
    accountId,
    canDelete,
    onExit,
}: {
    accountId: string;
    canDelete: boolean;
    onExit: () => void;
}) {
    const { t } = useI18n();
    const [startPayoutBankSetup] = usePayoutBankSetupMutation();
    const [completePayoutBankSetup] = useCompletePayoutBankSetupMutation();
    const [busy, setBusy] = useState(false);
    const [completedIntent, setCompletedIntent] = useState<string>();
    const [failed, setFailed] = useState(false);
    const [errorDetail, setErrorDetail] = useState("");
    const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY as string | undefined;

    async function linkBank() {
        if (!publishableKey || busy) return;
        setFailed(false);
        setErrorDetail("");
        setBusy(true);
        let stage = "Loading Stripe";
        try {
            let setupIntentId = completedIntent;
            if (!setupIntentId) {
                const stripe = await loadStripe(publishableKey);
                if (!stripe) throw new Error("Stripe could not load");
                stage = "Starting bank setup";
                const setup = await startPayoutBankSetup(accountId).unwrap();
                stage = "Connecting bank";
                const collected = await stripe.collectBankAccountForSetup({
                    clientSecret: setup.clientSecret,
                    params: {
                        payment_method_type: "us_bank_account",
                        payment_method_data: { billing_details: { name: setup.name } },
                    },
                });
                if (collected.error) throw collected.error;
                if (collected.setupIntent.status === "requires_payment_method") return;
                stage = "Confirming bank";
                const confirmed =
                    collected.setupIntent.status === "requires_confirmation"
                        ? await stripe.confirmUsBankAccountSetup(setup.clientSecret)
                        : collected;
                if (confirmed.error) throw confirmed.error;
                if (confirmed.setupIntent.status !== "succeeded")
                    throw new Error("Bank setup incomplete");
                setupIntentId = confirmed.setupIntent.id;
                setCompletedIntent(setupIntentId);
            }
            stage = "Saving bank account";
            await completePayoutBankSetup({ id: accountId, setupIntentId }).unwrap();
            onExit();
        } catch (error) {
            const failure = error as {
                message?: string;
                status?: number | string;
                data?: { message?: string; requestId?: string };
            };
            const message = failure?.data?.message ?? failure?.message ?? "";
            const reference = failure?.data?.requestId;
            setErrorDetail(
                [
                    stage,
                    message,
                    failure?.status ? `HTTP ${failure.status}` : "",
                    reference ? `Stripe request ${reference}` : "",
                ]
                    .filter(Boolean)
                    .join(" — "),
            );
            setFailed(true);
        } finally {
            setBusy(false);
        }
    }

    return (
        <CenteredPage
            title={t("accounts.externalAccountTitle")}
            description={t("accounts.externalAccountDescription")}
            backTo="/app"
            backLabel={t("accounts.allAccounts")}
        >
            {!publishableKey ? (
                <p role="status">{t("accounts.bankAccountUnavailable")}</p>
            ) : (
                <>
                    {failed && (
                        <div role="alert">
                            <p>{t("accounts.bankAccountLoadError")}</p>
                            {errorDetail && (
                                <p className="mt-2 text-sm text-muted-foreground">{errorDetail}</p>
                            )}
                        </div>
                    )}
                    <Button
                        className="w-full"
                        disabled={busy}
                        aria-busy={busy}
                        onClick={() => void linkBank()}
                    >
                        {t(failed ? "accounts.retry" : "accounts.bankAccountConnect")}
                    </Button>
                </>
            )}
            {canDelete && <AccountDeletionSection accountId={accountId} disabled={busy} />}
        </CenteredPage>
    );
}
