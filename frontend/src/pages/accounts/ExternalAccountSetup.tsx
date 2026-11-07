import { useI18n } from "@/shared/i18n/useI18n";
import { CenteredPage } from "@/shared/ui/centered-page";

const fields = [
    { label: "accounts.externalAccountHolder", placeholder: "Example Company" },
    { label: "accounts.externalAccountRouting", placeholder: "•••••••••" },
    { label: "accounts.externalAccountNumber", placeholder: "••••••••••••" },
] as const;

export default function ExternalAccountSetup() {
    const { t } = useI18n();

    return (
        <CenteredPage
            title={t("accounts.externalAccountTitle")}
            description={t("accounts.externalAccountDescription")}
            backTo="/app"
            backLabel={t("accounts.allAccounts")}
        >
            <fieldset disabled className="grid gap-4">
                {fields.map(({ label, placeholder }) => (
                    <label key={label} className="grid gap-2 text-sm font-medium">
                        {t(label)}
                        <input
                            className="h-10 rounded-md border border-border bg-background px-3 text-sm"
                            placeholder={placeholder}
                        />
                    </label>
                ))}
            </fieldset>
        </CenteredPage>
    );
}
