import * as Dialog from "@radix-ui/react-dialog";
import {
    ArrowLeft,
    ArrowLeftRight,
    ChartNoAxesColumn,
    Check,
    ChevronLeft,
    CreditCard,
    Landmark,
    LayoutDashboard,
    Settings,
    SunMoon,
    UserRound,
    Users,
    Wallet,
} from "lucide-react";
import { Suspense, useEffect, useRef, useState } from "react";
import { Link, Outlet, useLocation, useMatch, useParams } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { accountName, useAccountOnboardingQuery, useAccountQuery } from "@/features/accounts";
import { setTheme, type Theme } from "@/features/theme/store/themeSlice";
import { useI18n } from "@/shared/i18n/useI18n";
import { AccountSwitcher } from "@/shared/ui/account-switcher";
import { WorkspaceShell, WorkspaceSidebar } from "@/shared/ui/workspace-shell";

export default function AccountWorkspaceLayout() {
    const { accountId = "" } = useParams();
    const location = useLocation();
    const { t } = useI18n();
    const dispatch = useAppDispatch();
    const user = useAppSelector((state) => state.session.user);
    const theme = useAppSelector((state) => state.theme.theme);
    const [themeOpen, setThemeOpen] = useState(false);
    const themeCloseTimer = useRef<number | null>(null);
    const { data: account } = useAccountQuery(accountId, { skip: !accountId });
    const { currentData: onboarding } = useAccountOnboardingQuery(accountId, {
        skip: !accountId,
    });
    const name = account && (accountName(account) || t("accounts.untitled"));
    const accountPath = `/app/accounts/${accountId}`;
    const settingsOpen = useMatch(`${accountPath}/settings`);
    const initials =
        [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join("").toUpperCase() ||
        user?.email?.[0]?.toUpperCase() ||
        "?";

    function cancelThemeClose() {
        if (themeCloseTimer.current !== null) window.clearTimeout(themeCloseTimer.current);
        themeCloseTimer.current = null;
    }

    function closeThemeSoon() {
        cancelThemeClose();
        themeCloseTimer.current = window.setTimeout(() => setThemeOpen(false), 120);
    }

    useEffect(
        () => () => {
            if (themeCloseTimer.current !== null) window.clearTimeout(themeCloseTimer.current);
        },
        [],
    );

    if (onboarding?.status !== "COMPLETED") return <Outlet />;

    return (
        <WorkspaceShell
            sidebarToggleLabel={t("accounts.toggleSidebar")}
            navbar={t(settingsOpen ? "settings.title" : "accounts.home")}
            navbarActions={
                <div className="relative">
                    <Dialog.Root
                        modal={false}
                        onOpenChange={(open) => {
                            if (!open) {
                                cancelThemeClose();
                                setThemeOpen(false);
                            }
                        }}
                    >
                        <Dialog.Trigger
                            aria-label={t("accounts.profileMenu")}
                            className="grid size-9 place-items-center rounded-full bg-accent font-display text-xs font-bold text-brand-leaf hover:bg-secondary focus-visible:outline-2 focus-visible:outline-ring"
                        >
                            <span aria-hidden="true">{initials}</span>
                        </Dialog.Trigger>
                        <Dialog.Content
                            aria-describedby={undefined}
                            className="absolute top-full right-0 z-50 mt-2 w-56 rounded-xl border bg-popover p-1 text-popover-foreground shadow-xl motion-safe:animate-in motion-safe:animation-duration-150 motion-safe:slide-in-from-top-2 motion-safe:fade-in"
                        >
                            <Dialog.Title className="sr-only">
                                {t("accounts.profileMenu")}
                            </Dialog.Title>
                            <Dialog.Close asChild>
                                <Link
                                    to="/app/settings"
                                    state={{
                                        from: location.pathname + location.search + location.hash,
                                    }}
                                    className="flex min-h-8 items-center gap-2.5 rounded-md px-3 text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                                >
                                    <UserRound aria-hidden="true" size={16} />
                                    {t("accounts.userSettings")}
                                </Link>
                            </Dialog.Close>
                            <Dialog.Close asChild>
                                <Link
                                    to={`${accountPath}/settings`}
                                    className="flex min-h-8 items-center gap-2.5 rounded-md px-3 text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                                >
                                    <Settings aria-hidden="true" size={16} />
                                    {t("accounts.accountSettings")}
                                </Link>
                            </Dialog.Close>
                            <div role="separator" className="my-1 border-t border-border" />
                            <div
                                className="relative"
                                onPointerEnter={cancelThemeClose}
                                onPointerMove={() => setThemeOpen(true)}
                                onPointerLeave={closeThemeSoon}
                            >
                                <button
                                    type="button"
                                    aria-haspopup="true"
                                    aria-expanded={themeOpen}
                                    onClick={() => setThemeOpen(true)}
                                    className="flex min-h-8 w-full items-center gap-2.5 rounded-md px-3 text-left text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                                >
                                    <SunMoon aria-hidden="true" size={16} />
                                    <span className="flex-1">{t("accounts.theme")}</span>
                                    <ChevronLeft aria-hidden="true" size={14} />
                                </button>
                                {themeOpen && (
                                    <div
                                        role="group"
                                        aria-label={t("accounts.theme")}
                                        onPointerEnter={cancelThemeClose}
                                        className="absolute top-full right-0 z-10 w-40 rounded-xl border bg-popover p-1 shadow-xl motion-safe:animate-in motion-safe:animation-duration-150 motion-safe:fade-in sm:top-0 sm:right-full"
                                    >
                                        {(["system", "light", "dark"] as const).map(
                                            (option: Theme) => (
                                                <button
                                                    key={option}
                                                    type="button"
                                                    aria-pressed={theme === option}
                                                    onClick={() => {
                                                        cancelThemeClose();
                                                        dispatch(setTheme(option));
                                                        setThemeOpen(false);
                                                    }}
                                                    className="flex min-h-8 w-full items-center gap-2 rounded-md px-3 text-left text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                                                >
                                                    <span className="flex-1">
                                                        {t(`home.${option}`)}
                                                    </span>
                                                    {theme === option && (
                                                        <Check aria-hidden="true" size={15} />
                                                    )}
                                                </button>
                                            ),
                                        )}
                                    </div>
                                )}
                            </div>
                            <div role="separator" className="my-1 border-t border-border" />
                            <Dialog.Close asChild>
                                <Link
                                    to="/app"
                                    className="flex min-h-8 items-center gap-2.5 rounded-md px-3 text-sm hover:bg-accent focus-visible:outline-2 focus-visible:outline-ring"
                                >
                                    <ArrowLeft aria-hidden="true" size={16} />
                                    {t("accounts.allAccounts")}
                                </Link>
                            </Dialog.Close>
                            <div role="separator" className="my-1 border-t border-border" />
                            <button
                                type="button"
                                disabled
                                title={t("accounts.navComingSoon")}
                                className="flex min-h-8 w-full items-center gap-2.5 rounded-md px-3 text-sm text-muted-foreground"
                            >
                                <CreditCard aria-hidden="true" size={16} />
                                {t("accounts.subscription")}
                            </button>
                        </Dialog.Content>
                    </Dialog.Root>
                </div>
            }
            sidebar={(collapsed) => (
                <WorkspaceSidebar
                    collapsed={collapsed}
                    label={t("accounts.accountOverview")}
                    comingSoon={t("accounts.navComingSoon")}
                    upgradeLabel={t("accounts.upgrade")}
                    accountSwitcher={
                        name ? (
                            <AccountSwitcher
                                accountId={accountId}
                                name={name}
                                collapsed={collapsed}
                            />
                        ) : undefined
                    }
                    groups={[
                        {
                            label: t("accounts.navWorkspace"),
                            links: [
                                {
                                    to: accountPath,
                                    label: t("accounts.overview"),
                                    icon: LayoutDashboard,
                                },
                                { label: t("accounts.navTransactions"), icon: ArrowLeftRight },
                                { label: t("accounts.navCards"), icon: CreditCard },
                                { label: t("accounts.navPayouts"), icon: Wallet },
                                { label: t("accounts.navBankAccounts"), icon: Landmark },
                                { label: t("accounts.navReports"), icon: ChartNoAxesColumn },
                            ],
                        },
                        {
                            label: t("accounts.navManagement"),
                            links: [{ label: t("accounts.navTeam"), icon: Users }],
                        },
                    ]}
                />
            )}
        >
            <Suspense
                fallback={
                    <p role="status" className="text-sm text-muted-foreground">
                        {t("route.loading")}
                    </p>
                }
            >
                <Outlet />
            </Suspense>
        </WorkspaceShell>
    );
}
