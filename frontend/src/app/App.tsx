import Router from "./Router";
import { SessionProvider } from "./providers/SessionProvider";
import { ThemeProvider } from "./providers/ThemeProvider";
import { ErrorBoundary } from "./providers/ErrorBoundary";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
export default function App() {
    return (
        <I18nProvider>
            <ErrorBoundary>
                <ThemeProvider>
                    <SessionProvider>
                        <Router />
                    </SessionProvider>
                </ThemeProvider>
            </ErrorBoundary>
        </I18nProvider>
    );
}
