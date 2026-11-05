import { Component, type ReactNode } from "react";
import { useI18n } from "@/shared/i18n/useI18n";

function ErrorScreen() {
    const { t } = useI18n();
    return (
        <main className="grid min-h-screen place-content-center gap-4 p-6">
            <h1>{t("error.title")}</h1>
            <p>{t("error.reloadHelp")}</p>
            <button onClick={() => window.location.reload()}>{t("error.reload")}</button>
        </main>
    );
}

export class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
    state = { failed: false };

    static getDerivedStateFromError() {
        return { failed: true };
    }

    render() {
        if (this.state.failed) return <ErrorScreen />;
        return this.props.children;
    }
}
