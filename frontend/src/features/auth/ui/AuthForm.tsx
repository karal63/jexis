import type { ComponentProps } from "react";
import { Button } from "@/shared/ui/button";
import { useI18n } from "@/shared/i18n/useI18n";

export function AuthForm(props: ComponentProps<"form">) {
    return <form {...props} noValidate className="space-y-1" />;
}
export function AuthSubmit({ loading, children }: { loading: boolean; children: React.ReactNode }) {
    const { t } = useI18n();
    return (
        <Button type="submit" disabled={loading} aria-busy={loading} className="mt-2 w-full">
            {loading ? t("common.wait") : children}
        </Button>
    );
}
export function FormError({ message }: { message?: string }) {
    return message ? (
        <p
            role="alert"
            className="mb-3 rounded-md border border-danger-line bg-danger-bg px-3 py-2.5 text-sm text-danger"
        >
            {message}
        </p>
    ) : null;
}
