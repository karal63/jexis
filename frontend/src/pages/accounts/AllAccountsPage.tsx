import { useEffect, useState } from "react";
import { Building2, ChevronRight, Cog, Leaf, LogOut, Search } from "lucide-react";
import { Link } from "react-router-dom";
import { useLogoutMutation } from "@/features/auth";
import { accountName, useMyAccountsQuery } from "@/features/accounts";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { useI18n } from "@/shared/i18n/useI18n";

export default function AllAccountsPage() {
    const { t } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const dispatch = useAppDispatch();
    const [logout, { isLoading: signingOut }] = useLogoutMutation();
    const [logoutError, setLogoutError] = useState<unknown>(null);
    const [draftSearch, setDraftSearch] = useState("");
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(1);
    const {
        currentData: data,
        error,
        isLoading,
        isFetching,
        refetch,
    } = useMyAccountsQuery({ page, search });

    useEffect(() => {
        const timer = window.setTimeout(() => {
            setPage(1);
            setSearch(draftSearch.trim());
        }, 300);
        return () => window.clearTimeout(timer);
    }, [draftSearch]);

    async function signOut() {
        setLogoutError(null);
        try {
            await logout().unwrap();
            dispatch(sessionEnded());
        } catch (error) {
            setLogoutError(error);
        }
    }

    const accounts = data?.items ?? [];
    const userName =
        [user?.firstName, user?.lastName].filter(Boolean).join(" ").trim() || user?.email || "";
    const initials =
        [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join("").toUpperCase() ||
        user?.email?.[0]?.toUpperCase() ||
        "?";

    return (
        <main className="flex min-h-dvh flex-col items-center bg-card px-5 pb-8 text-foreground">
            <Link
                to="/app"
                className="mt-[6vh] inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-.08em]"
                aria-label={t("accounts.allAccounts")}
            >
                <span className="grid size-7 place-items-center">
                    <Leaf size={18} strokeWidth={2.5} />
                </span>
                jexis
            </Link>

            <section
                className="mt-[clamp(52px,10vh,112px)] w-full max-w-[460px] pb-10"
                aria-labelledby="accounts-heading"
            >
                <div className="text-center">
                    <h1
                        id="accounts-heading"
                        className="font-display text-[27px] font-bold tracking-[-.045em]"
                    >
                        {t("accounts.allAccounts")}
                    </h1>
                    <p className="mt-2 text-sm text-muted-foreground">{t("accounts.intro")}</p>
                </div>

                <div role="search" className="relative mt-9">
                    <label htmlFor="account-search" className="sr-only">
                        {t("accounts.searchLabel")}
                    </label>
                    <Search
                        aria-hidden="true"
                        size={17}
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                    />
                    <input
                        id="account-search"
                        type="search"
                        value={draftSearch}
                        onChange={(event) => setDraftSearch(event.target.value)}
                        placeholder={t("accounts.searchPlaceholder")}
                        className="h-11 w-full rounded-md border border-border bg-card pl-10 pr-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    />
                </div>

                {isLoading || (isFetching && !data) ? (
                    <p role="status" className="py-14 text-center text-sm text-muted-foreground">
                        {t("accounts.loading")}
                    </p>
                ) : error ? (
                    <div className="py-12 text-center">
                        <p role="alert" className="text-sm text-danger">
                            {getApiError(error, t).message}
                        </p>
                        <Button variant="secondary" className="mt-5" onClick={() => refetch()}>
                            {t("accounts.retry")}
                        </Button>
                    </div>
                ) : accounts.length === 0 ? (
                    <div className="py-12 text-center">
                        <span className="mx-auto grid size-11 place-items-center rounded-lg bg-accent text-brand-leaf">
                            <Building2 size={21} />
                        </span>
                        <h2 className="mt-5 font-display text-base font-bold">
                            {t(search ? "accounts.noMatches" : "accounts.emptyTitle")}
                        </h2>
                        <p className="mt-2 text-sm leading-6 text-muted-foreground">
                            {t(search ? "accounts.noMatchesText" : "accounts.emptyText")}
                        </p>
                    </div>
                ) : (
                    <>
                        <div
                            className="mt-5 max-h-[min(54vh,540px)] overflow-y-auto rounded-lg border border-border"
                            aria-busy={isFetching}
                        >
                            {accounts.map((account) => {
                                const name = accountName(account) || t("accounts.untitled");
                                return (
                                    <Link
                                        key={account.id}
                                        to={`/app/accounts/${account.id}`}
                                        aria-label={t("accounts.openNamed", { name })}
                                        className="group flex min-h-20 items-center gap-4 border-b border-border px-4 py-3 transition-colors last:border-b-0 hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ring"
                                    >
                                        <span className="grid size-10 shrink-0 place-items-center rounded-md bg-accent text-brand-leaf">
                                            <Building2 aria-hidden="true" size={19} />
                                        </span>
                                        <span className="min-w-0 flex-1">
                                            <span className="block truncate text-sm font-semibold">
                                                {name}
                                            </span>
                                            {account.email && name !== account.email && (
                                                <span className="mt-0.5 block truncate text-xs text-muted-foreground">
                                                    {account.email}
                                                </span>
                                            )}
                                        </span>
                                        {account.owner?.id === user?.id && (
                                            <span className="hidden shrink-0 text-xs text-muted-foreground sm:block">
                                                {t("accounts.owner")}
                                            </span>
                                        )}
                                        <ChevronRight
                                            aria-hidden="true"
                                            size={17}
                                            className="shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5"
                                        />
                                    </Link>
                                );
                            })}
                        </div>
                        {data && data.totalPages > 1 && (
                            <nav
                                aria-label={t("accounts.pagination")}
                                className="mt-6 flex items-center justify-between gap-3"
                            >
                                <Button
                                    variant="secondary"
                                    size="small"
                                    onClick={() => setPage((value) => Math.max(1, value - 1))}
                                    disabled={page === 1 || isFetching}
                                >
                                    {t("accounts.previous")}
                                </Button>
                                <span className="text-xs text-muted-foreground">
                                    {t("accounts.page", { page, total: data.totalPages })}
                                </span>
                                <Button
                                    variant="secondary"
                                    size="small"
                                    onClick={() =>
                                        setPage((value) => Math.min(data.totalPages, value + 1))
                                    }
                                    disabled={page === data.totalPages || isFetching}
                                >
                                    {t("accounts.next")}
                                </Button>
                            </nav>
                        )}
                    </>
                )}
            </section>

            <footer className="mt-auto w-full max-w-[460px] border-t border-border pt-4">
                {logoutError != null && (
                    <p role="alert" className="mb-3 text-center text-xs text-danger">
                        {getApiError(logoutError, t).message}
                    </p>
                )}
                <div className="flex items-center gap-3">
                    <span
                        aria-hidden="true"
                        className="grid size-9 shrink-0 place-items-center rounded-full bg-accent font-display text-xs font-bold text-brand-leaf"
                    >
                        {initials}
                    </span>
                    <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-semibold">{userName}</p>
                        <p className="truncate text-xs text-muted-foreground">{user?.email}</p>
                    </div>
                    <button
                        type="button"
                        onClick={signOut}
                        disabled={signingOut}
                        className="inline-flex min-h-9 shrink-0 items-center gap-2 rounded-md px-2.5 text-xs font-semibold text-muted-foreground hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                    >
                        <LogOut aria-hidden="true" size={14} />
                        {signingOut ? t("home.signingOut") : t("home.signOut")}
                    </button>
                    <Link
                        to="/app/settings"
                        aria-label={t("settings.title")}
                        title={t("settings.title")}
                        className="grid size-9 shrink-0 place-items-center rounded-md border border-border text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    >
                        <Cog aria-hidden="true" size={16} />
                    </Link>
                </div>
            </footer>
        </main>
    );
}
