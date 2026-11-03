import { Component, type ReactNode } from "react";

export class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
    state = { failed: false };

    static getDerivedStateFromError() {
        return { failed: true };
    }

    render() {
        if (this.state.failed)
            return (
                <main className="grid min-h-screen place-content-center gap-4 p-6">
                    <h1>Something went wrong</h1>
                    <p>Please reload the page to try again.</p>
                    <button onClick={() => window.location.reload()}>Reload page</button>
                </main>
            );
        return this.props.children;
    }
}
