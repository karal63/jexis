import {
    ArrowLeftRight,
    ChartNoAxesColumn,
    CreditCard,
    Landmark,
    LayoutDashboard,
    Users,
    Wallet,
} from "lucide-react";
import { Link, Outlet, useParams } from "react-router-dom";
import { useAppSelector } from "@/app/hooks";
import { accountName, useAccountOnboardingQuery, useAccountQuery } from "@/features/accounts";
import { useI18n } from "@/shared/i18n/useI18n";
import { AccountSwitcher } from "@/shared/ui/account-switcher";
import { WorkspaceShell, WorkspaceSidebar } from "@/shared/ui/workspace-shell";

export default function AccountWorkspaceLayout() {
    const { accountId = "" } = useParams();
    const { t } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const { data: account } = useAccountQuery(accountId, { skip: !accountId });
    const { currentData: onboarding } = useAccountOnboardingQuery(accountId, {
        skip: !accountId,
    });
    const name = account && (accountName(account) || t("accounts.untitled"));
    const accountPath = `/app/accounts/${accountId}`;
    const initials =
        [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join("").toUpperCase() ||
        user?.email?.[0]?.toUpperCase() ||
        "?";

    if (onboarding?.status !== "COMPLETED") return <Outlet />;

    return (
        <WorkspaceShell
            sidebarToggleLabel={t("accounts.toggleSidebar")}
            navbar={t("accounts.home")}
            navbarActions={
                <Link
                    to="/app/settings"
                    aria-label={t("settings.title")}
                    title={t("settings.title")}
                    className="grid size-9 place-items-center rounded-full bg-accent font-display text-xs font-bold text-brand-leaf hover:bg-secondary focus-visible:outline-2 focus-visible:outline-ring"
                >
                    <span aria-hidden="true">{initials}</span>
                </Link>
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
            <Outlet />
        </WorkspaceShell>
    );
}
