import {
    ArrowLeft,
    CreditCard,
    KeyRound,
    Leaf,
    MonitorSmartphone,
    SunMoon,
    UserRound,
} from "lucide-react";
import { useRef, useState, type KeyboardEvent } from "react";
import { Link } from "react-router-dom";
import { useAppSelector } from "@/app/hooks";
import { useI18n } from "@/shared/i18n/useI18n";
import { cn } from "@/shared/lib/utils";
import { PersonalInfoForm } from "@/features/profile";
import { AppearanceSettings } from "./AppearanceSettings";
import { PasswordSettings } from "./PasswordSettings";
const SETTINGS_TABS = [
    { id: "profile", label: "settings.publicProfile", Icon: UserRound, Panel: PersonalInfoForm },
    { id: "appearance", label: "settings.appearanceTab", Icon: SunMoon, Panel: AppearanceSettings },
    { id: "password", label: "settings.passwordTab", Icon: KeyRound, Panel: PasswordSettings },
] as const;
type SettingsTab = (typeof SETTINGS_TABS)[number]["id"];
const tabOrder = SETTINGS_TABS.map((tab) => tab.id);

export default function SettingsPage() {
    const { t } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const [activeTab, setActiveTab] = useState<SettingsTab>("profile");
    const tabRefs = useRef<Partial<Record<SettingsTab, HTMLButtonElement | null>>>({});

    function handleTabKeyDown(event: KeyboardEvent<HTMLButtonElement>, tab: SettingsTab) {
        const currentIndex = tabOrder.indexOf(tab);
        let nextIndex: number;
        switch (event.key) {
            case "ArrowRight":
            case "ArrowDown":
                nextIndex = (currentIndex + 1) % tabOrder.length;
                break;
            case "ArrowLeft":
            case "ArrowUp":
                nextIndex = (currentIndex - 1 + tabOrder.length) % tabOrder.length;
                break;
            case "Home":
                nextIndex = 0;
                break;
            case "End":
                nextIndex = tabOrder.length - 1;
                break;
            default:
                return;
        }
        event.preventDefault();
        const nextTab = tabOrder[nextIndex];
        setActiveTab(nextTab);
        tabRefs.current[nextTab]?.focus();
    }

    const unavailableTabs = [
        { label: t("settings.billingTab"), Icon: CreditCard },
        { label: t("settings.sessionsTab"), Icon: MonitorSmartphone },
    ];

    return (
        <main className="min-h-dvh bg-card px-5 pb-12 text-foreground sm:px-8">
            <div className="mx-auto w-full max-w-[1200px]">
                <header className="flex items-center justify-between gap-4 pt-5 sm:pt-6">
                    <Link
                        to="/app"
                        className="inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-.08em]"
                        aria-label={t("accounts.allAccounts")}
                    >
                        <span className="grid size-7 place-items-center">
                            <Leaf size={18} strokeWidth={2.5} />
                        </span>
                        jexis
                    </Link>
                    <Link
                        to="/app"
                        className="inline-flex items-center gap-2 text-xs font-semibold text-brand-leaf hover:underline"
                    >
                        <ArrowLeft aria-hidden="true" size={14} />
                        {t("accounts.allAccounts")}
                    </Link>
                </header>

                <div className="mt-8 pb-10 sm:mt-10">
                    <h1 className="font-display text-[27px] font-bold tracking-[-.045em]">
                        {t("settings.title")}
                    </h1>
                    <p className="mt-2 text-sm text-muted-foreground">{t("settings.intro")}</p>

                    <div className="mt-6 grid min-w-0 gap-8 lg:grid-cols-[240px_minmax(0,1fr)] lg:gap-12">
                        <div className="flex min-w-0 gap-2 overflow-x-auto pb-2 lg:flex-col lg:overflow-visible">
                            <div
                                role="tablist"
                                aria-label={t("settings.sections")}
                                className="flex shrink-0 gap-2 lg:w-full lg:flex-col"
                            >
                                {SETTINGS_TABS.map(({ id, label, Icon }) => (
                                    <button
                                        key={id}
                                        ref={(element) => {
                                            tabRefs.current[id] = element;
                                        }}
                                        id={`settings-tab-${id}`}
                                        type="button"
                                        role="tab"
                                        aria-selected={activeTab === id}
                                        aria-controls={`settings-panel-${id}`}
                                        tabIndex={activeTab === id ? 0 : -1}
                                        onClick={() => setActiveTab(id)}
                                        onKeyDown={(event) => handleTabKeyDown(event, id)}
                                        className={cn(
                                            "flex min-h-11 shrink-0 items-center gap-3 rounded-lg px-4 text-left text-sm font-semibold outline-none transition-colors focus-visible:ring-2 focus-visible:ring-ring lg:w-full",
                                            activeTab === id
                                                ? "bg-accent text-brand-leaf"
                                                : "text-muted-foreground hover:bg-accent/60 hover:text-foreground",
                                        )}
                                    >
                                        <Icon aria-hidden="true" size={18} className="shrink-0" />
                                        {t(label)}
                                    </button>
                                ))}
                            </div>
                            <div
                                aria-hidden="true"
                                className="mx-1 hidden border-t border-border lg:block"
                            />
                            <div className="flex shrink-0 gap-2 lg:w-full lg:flex-col">
                                {unavailableTabs.map(({ label, Icon }) => (
                                    <button
                                        key={label}
                                        type="button"
                                        disabled
                                        className="flex min-h-11 shrink-0 cursor-not-allowed items-center gap-3 rounded-lg px-4 text-left text-sm text-muted-foreground opacity-50 lg:w-full"
                                    >
                                        <Icon aria-hidden="true" size={18} className="shrink-0" />
                                        {label}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="min-w-0">
                            {SETTINGS_TABS.map(({ id, Panel }) => (
                                <div
                                    key={id}
                                    id={`settings-panel-${id}`}
                                    role="tabpanel"
                                    aria-labelledby={`settings-tab-${id}`}
                                    tabIndex={0}
                                    hidden={activeTab !== id}
                                >
                                    <Panel key={user?.id ?? "anonymous"} />
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </main>
    );
}
