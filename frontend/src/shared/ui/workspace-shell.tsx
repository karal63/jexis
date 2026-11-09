import { Leaf, PanelLeft, Zap, type LucideIcon } from "lucide-react";
import { useId, useState, useSyncExternalStore, type ReactNode } from "react";
import { NavLink } from "react-router-dom";
import { cn } from "@/shared/lib/utils";

export function WorkspaceNavbar({
    children,
    leading,
    actions,
}: {
    children: ReactNode;
    leading?: ReactNode;
    actions?: ReactNode;
}) {
    return (
        <header className="col-start-1 row-start-1 flex min-w-0 items-center justify-between gap-4 border-b px-4 py-2.5 sm:px-6 md:col-start-2">
            <div className="flex min-w-0 items-center gap-3">
                {leading}
                <div className="min-w-0 truncate text-sm font-semibold">{children}</div>
            </div>
            {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
        </header>
    );
}

type WorkspaceLink = { to?: string; label: string; icon: LucideIcon };

const sidebarItem = "flex min-h-10 items-center gap-2.5 rounded-md px-3 text-sm font-medium";

function subscribeToDesktop(callback: () => void) {
    const media = window.matchMedia("(min-width: 768px)");
    media.addEventListener("change", callback);
    return () => media.removeEventListener("change", callback);
}

function isDesktopViewport() {
    return window.matchMedia("(min-width: 768px)").matches;
}

export function WorkspaceSidebar({
    label,
    comingSoon,
    accountSwitcher,
    upgradeLabel,
    groups,
    collapsed = false,
}: {
    label: string;
    comingSoon: string;
    accountSwitcher?: ReactNode;
    upgradeLabel: string;
    groups: { label?: string; links: WorkspaceLink[] }[];
    collapsed?: boolean;
}) {
    return (
        <aside
            className={cn(
                "flex max-h-[40dvh] flex-col border-b bg-sidebar px-3 py-2 md:h-full md:max-h-none md:border-r md:border-b-0 md:py-4",
                collapsed ? "md:w-14 md:px-1.5" : "md:w-60",
            )}
        >
            <div
                className={cn(
                    "mb-6 hidden items-center gap-2 px-3 font-display text-xl font-bold tracking-[-.08em] md:flex",
                    collapsed && "md:justify-center md:px-0",
                )}
            >
                <span className="grid size-7 shrink-0 place-items-center rounded-[9px_9px_9px_3px] bg-brand-green text-primary-foreground">
                    <Leaf aria-hidden="true" size={15} />
                </span>
                <span className={cn(collapsed && "md:sr-only")}>jexis</span>
            </div>
            {accountSwitcher && <div className="mb-5">{accountSwitcher}</div>}
            <nav
                aria-label={label}
                className="min-h-0 flex-1 space-y-3 overflow-y-auto md:space-y-5"
            >
                {groups.map(({ label, links }) => (
                    <div key={label}>
                        {label && (
                            <p
                                className={cn(
                                    "mb-1 px-3 text-xs font-medium text-muted-foreground",
                                    collapsed && "md:sr-only",
                                )}
                            >
                                {label}
                            </p>
                        )}
                        <ul className="grid grid-cols-2 gap-1 md:grid-cols-1">
                            {links.map(({ to, label, icon: Icon }) => (
                                <li key={label}>
                                    {to ? (
                                        <NavLink
                                            to={to}
                                            end
                                            title={collapsed ? label : undefined}
                                            className={({ isActive }) =>
                                                cn(
                                                    sidebarItem,
                                                    collapsed && "md:justify-center md:px-0",
                                                    "focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-ring",
                                                    isActive
                                                        ? "bg-[var(--sidebar-active)] text-[var(--sidebar-active-foreground)]"
                                                        : "text-muted-foreground hover:bg-accent hover:text-foreground",
                                                )
                                            }
                                        >
                                            <Icon
                                                aria-hidden="true"
                                                className="shrink-0"
                                                size={17}
                                            />
                                            <span className={cn(collapsed && "md:sr-only")}>
                                                {label}
                                            </span>
                                        </NavLink>
                                    ) : (
                                        <span
                                            role="link"
                                            aria-disabled="true"
                                            title={`${label} · ${comingSoon}`}
                                            className={cn(
                                                sidebarItem,
                                                collapsed && "md:justify-center md:px-0",
                                                "cursor-default text-muted-foreground/80",
                                            )}
                                        >
                                            <Icon
                                                aria-hidden="true"
                                                className="shrink-0"
                                                size={17}
                                            />
                                            <span className={cn(collapsed && "md:sr-only")}>
                                                {label}
                                            </span>
                                        </span>
                                    )}
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </nav>
            <button
                type="button"
                className={cn(
                    "mt-4 flex min-h-10 w-full shrink-0 cursor-pointer items-center justify-center gap-2 rounded-md bg-primary px-3 text-sm font-semibold text-primary-foreground shadow-sm transition-colors hover:bg-brand-green-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
                    collapsed && "md:px-0",
                )}
            >
                <Zap aria-hidden="true" size={16} />
                <span className={cn(collapsed && "md:sr-only")}>{upgradeLabel}</span>
            </button>
        </aside>
    );
}

export function WorkspaceShell({
    navbar,
    navbarActions,
    sidebar,
    sidebarToggleLabel,
    children,
}: {
    navbar: ReactNode;
    navbarActions?: ReactNode;
    sidebar: (collapsed: boolean) => ReactNode;
    sidebarToggleLabel: string;
    children: ReactNode;
}) {
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const sidebarId = useId();
    const isDesktop = useSyncExternalStore(subscribeToDesktop, isDesktopViewport, () => false);

    return (
        <div
            data-workspace-shell
            className={cn(
                "grid h-dvh grid-cols-[minmax(0,1fr)] grid-rows-[auto_auto_minmax(0,1fr)] bg-background text-foreground transition-[grid-template-columns] duration-200 motion-reduce:transition-none md:grid-rows-[auto_minmax(0,1fr)]",
                sidebarOpen
                    ? "md:grid-cols-[15rem_minmax(0,1fr)]"
                    : "md:grid-cols-[3.5rem_minmax(0,1fr)]",
            )}
        >
            <WorkspaceNavbar
                leading={
                    <button
                        type="button"
                        aria-label={sidebarToggleLabel}
                        aria-controls={sidebarId}
                        aria-expanded={sidebarOpen}
                        title={sidebarToggleLabel}
                        onClick={() => setSidebarOpen((open) => !open)}
                        className="grid size-9 shrink-0 place-items-center rounded-md border text-muted-foreground hover:bg-accent hover:text-foreground focus-visible:outline-2 focus-visible:outline-ring"
                    >
                        <PanelLeft aria-hidden="true" size={17} />
                    </button>
                }
                actions={navbarActions}
            >
                {navbar}
            </WorkspaceNavbar>
            <div
                id={sidebarId}
                inert={!sidebarOpen && !isDesktop}
                className={cn(
                    "col-start-1 row-start-2 grid min-h-0 overflow-hidden transition-[grid-template-rows] duration-200 motion-reduce:transition-none md:row-span-2 md:row-start-1 md:grid-rows-[minmax(0,1fr)] md:transition-none",
                    sidebarOpen ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
                )}
            >
                <div className="min-h-0 overflow-hidden">{sidebar(!sidebarOpen)}</div>
            </div>
            <main className="col-start-1 row-start-3 overflow-y-auto px-4 py-8 sm:px-6 md:col-start-2 md:row-start-2 lg:px-10 lg:py-12">
                <div className="mx-auto w-full max-w-6xl">{children}</div>
            </main>
        </div>
    );
}
