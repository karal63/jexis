import Router from "./Router";
import { SessionProvider } from "./providers/SessionProvider";
import { ThemeProvider } from "./providers/ThemeProvider";
import { ErrorBoundary } from "./providers/ErrorBoundary";
export default function App() {
    return (
        <ErrorBoundary>
            <ThemeProvider>
                <SessionProvider>
                    <Router />
                </SessionProvider>
            </ThemeProvider>
        </ErrorBoundary>
    );
}
