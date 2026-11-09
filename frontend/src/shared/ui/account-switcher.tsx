import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { ArrowLeft, Building2, Check, ChevronsUpDown } from "lucide-react";
import { Link } from "react-router-dom";
import { accountName, useMyAccountsQuery } from "@/features/accounts";
import { useI18n } from "@/shared/i18n/useI18n";
import { cn } from "@/shared/lib/utils";

export function AccountSwitcher({
    accountId,
    name,
    collapsed,
}: {
    accountId: string;
    name: string;
    collapsed: boolean;
}) {
    const { t } = useI18n();
    const [open, setOpen] = useState(false);
    const [page, setPage] = useState(1);
    const [position, setPosition] = useState({ top: 0, left: 0 });
    const trigger = useRef<HTMLButtonElement>(null);
    const panel = useRef<HTMLDivElement>(null);
    const {
        currentData: data,
        error,
        isFetching,
        refetch,
    } = useMyAccountsQuery({ page, search: "" }, { skip: !open });

    useEffect(() => {
        if (!open) return;
        panel.current?.focus();
        function dismiss(event: PointerEvent) {
            if (
                !panel.current?.contains(event.target as Node) &&
                !trigger.current?.contains(event.target as Node)
            ) {
                setOpen(false);
            }
        }
        function escape(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setOpen(false);
                trigger.current?.focus();
            }
        }
        function closeOnResize() {
            setOpen(false);
        }
        document.addEventListener("pointerdown", dismiss);
        document.addEventListener("keydown", escape);
        window.addEventListener("resize", closeOnResize);
        return () => {
            document.removeEventListener("pointerdown", dismiss);
            document.removeEventListener("keydown", escape);
            window.removeEventListener("resize", closeOnResize);
        };
    }, [open]);

    function toggle() {
        if (!open && trigger.current) {
            const bounds = trigger.current.getBoundingClientRect();
            const width = Math.min(320, window.innerWidth - 24);
            const rightSide = bounds.right + width + 8 <= window.innerWidth;
            setPosition({
                top: rightSide
                    ? Math.min(bounds.top, window.innerHeight - 320)
                    : Math.min(bounds.bottom + 8, window.innerHeight - 320),
                left: rightSide ? bounds.right + 8 : Math.max(12, bounds.left),
            });
        }
        setOpen((value) => !value);
    }

    return (
        <>
            <button
                ref={trigger}
                type="button"
                onClick={toggle}
                aria-label={t("accounts.switchAccount")}
                aria-expanded={open}
                aria-haspopup="dialog"
                title={collapsed ? name : undefined}
                className={cn(
                    "flex w-full items-center gap-2.5 rounded-lg p-2 text-left hover:bg-accent focus-visible:bg-accent focus-visible:outline-2 focus-visible:outline-ring",
                    collapsed && "md:justify-center md:p-2",
                )}
            >
                <span className="grid size-8 shrink-0 place-items-center rounded-md">
                    <Building2 aria-hidden="true" size={17} className="text-brand-leaf" />
                </span>
                <span
                    className={cn(
                        "flex min-w-0 flex-1 flex-col gap-0.5",
                        collapsed && "md:sr-only",
                    )}
                >
                    <span className="truncate text-sm font-semibold">{name}</span>
                    <span className="text-[11px] font-medium tracking-wide text-muted-foreground">
                        {t("accounts.freePlan")}
                    </span>
                </span>
                <ChevronsUpDown
                    aria-hidden="true"
                    size={15}
                    className={cn("shrink-0 text-muted-foreground", collapsed && "md:hidden")}
                />
            </button>
            {open &&
                createPortal(
                    <div
                        ref={panel}
                        role="dialog"
                        tabIndex={-1}
                        aria-label={t("accounts.switchAccount")}
                        className="fixed z-50 max-h-[min(70dvh,440px)] w-[min(320px,calc(100vw-24px))] overflow-y-auto rounded-xl border bg-popover p-2 text-popover-foreground shadow-2xl"
                        style={{ top: Math.max(12, position.top), left: position.left }}
                    >
                        <p className="px-3 py-2 text-xs font-medium text-muted-foreground">
                            {t("accounts.switchAccount")}
                        </p>
                        {isFetching && !data && (
                            <p role="status" className="px-3 py-3 text-sm text-muted-foreground">
                                {t("accounts.loading")}
                            </p>
                        )}
                        {error && (
                            <button
                                type="button"
                                onClick={() => void refetch()}
                                className="w-full rounded-md px-3 py-2 text-left text-sm text-danger hover:bg-accent"
                            >
                                {t("accounts.retry")}
                            </button>
                        )}
                        {data?.items.map((account) => (
                            <Link
                                key={account.id}
                                to={"/app/accounts/" + account.id}
                                onClick={() => setOpen(false)}
                                aria-current={account.id === accountId ? "page" : undefined}
                                className="flex min-h-11 items-center gap-3 rounded-md px-3 text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                            >
                                <Building2
                                    aria-hidden="true"
                                    size={17}
                                    className="shrink-0 text-muted-foreground"
                                />
                                <span className="min-w-0 flex-1 truncate">
                                    {accountName(account) || t("accounts.untitled")}
                                </span>
                                {account.id === accountId && <Check aria-hidden="true" size={16} />}
                            </Link>
                        ))}
                        {data && data.totalPages > 1 && (
                            <div className="mt-2 flex items-center justify-between gap-2 border-t px-2 pt-2">
                                <button
                                    type="button"
                                    disabled={page === 1 || isFetching}
                                    onClick={() => setPage((value) => value - 1)}
                                    className="rounded-md px-2 py-1 text-xs hover:bg-accent disabled:opacity-40"
                                >
                                    {t("accounts.previous")}
                                </button>
                                <span className="text-xs text-muted-foreground">
                                    {t("accounts.page", { page, total: data.totalPages })}
                                </span>
                                <button
                                    type="button"
                                    disabled={page === data.totalPages || isFetching}
                                    onClick={() => setPage((value) => value + 1)}
                                    className="rounded-md px-2 py-1 text-xs hover:bg-accent disabled:opacity-40"
                                >
                                    {t("accounts.next")}
                                </button>
                            </div>
                        )}
                        <div className="mt-2 border-t pt-2">
                            <Link
                                to="/app"
                                onClick={() => setOpen(false)}
                                className="flex min-h-10 items-center gap-3 rounded-md px-3 text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                            >
                                <ArrowLeft aria-hidden="true" size={17} />
                                {t("accounts.allAccounts")}
                            </Link>
                        </div>
                    </div>,
                    document.body,
                )}
        </>
    );
}
