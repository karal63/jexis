import { useEffect, type ReactNode } from "react";
import { useAppSelector } from "../hooks";
export function ThemeProvider({ children }: { children: ReactNode }) {
    const theme = useAppSelector((state) => state.theme.theme);

    useEffect(() => {
        const media = window.matchMedia("(prefers-color-scheme: dark)");
        const apply = () => {
            document.documentElement.dataset.theme =
                theme === "system" ? (media.matches ? "dark" : "light") : theme;
        };
        apply();
        try {
            localStorage.setItem("theme", theme);
        } catch {
            /* Storage may be unavailable. */
        }
        media.addEventListener("change", apply);
        return () => media.removeEventListener("change", apply);
    }, [theme]);
    return children;
}
