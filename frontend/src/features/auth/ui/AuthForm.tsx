import type { ComponentProps } from "react";
import { Button } from "@/shared/ui/button";

export function AuthForm(props: ComponentProps<"form">) {
    return <form {...props} noValidate className="space-y-1" />;
}
export function AuthSubmit({ loading, children }: { loading: boolean; children: React.ReactNode }) {
    return (
        <Button type="submit" disabled={loading} aria-busy={loading} className="mt-2 w-full">
            {loading ? "Please wait…" : children}
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
