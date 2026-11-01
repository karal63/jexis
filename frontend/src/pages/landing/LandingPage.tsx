import {
    ArrowRight,
    ArrowUpRight,
    CreditCard,
    Leaf,
    LockKeyhole,
    Menu,
    Wallet,
} from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/shared/ui/button";

const transactions = [
    {
        initials: "L",
        merchant: "Linear",
        detail: "Software · Today",
        amount: "−€24.00",
        mark: "bg-violet-100 text-violet-800",
    },
    {
        initials: "A",
        merchant: "Airbnb",
        detail: "Travel · Yesterday",
        amount: "−€284.50",
        mark: "bg-rose-100 text-rose-700",
    },
    {
        initials: "F",
        merchant: "Fieldwork Studio",
        detail: "Client payment · Yesterday",
        amount: "+€2,450.00",
        mark: "bg-emerald-100 text-emerald-800",
    },
];

function Wordmark() {
    return (
        <Link
            to="/"
            className="inline-flex items-center gap-2 font-display text-xl font-bold tracking-[-.08em] text-foreground"
            aria-label="Jexis home"
        >
            <span className="grid size-7 place-items-center rounded-[9px_9px_9px_3px] bg-brand-green text-primary-foreground">
                <Leaf size={15} />
            </span>
            jexis
        </Link>
    );
}

function Header() {
    const [menuOpen, setMenuOpen] = useState(false);
    return (
        <header className="relative mx-auto flex max-w-7xl items-center justify-between px-5 py-5 sm:px-8">
            <Wordmark />
            <button
                onClick={() => setMenuOpen(!menuOpen)}
                aria-expanded={menuOpen}
                aria-controls="primary-navigation"
                aria-label={menuOpen ? "Close navigation" : "Open navigation"}
                className="grid size-9 place-items-center rounded-md border border-border text-brand-green md:hidden"
            >
                <Menu size={17} />
            </button>
            <nav
                id="primary-navigation"
                className={`${menuOpen ? "flex" : "hidden"} absolute left-4 right-4 top-[65px] z-10 flex-col gap-1 rounded-lg border border-border bg-card p-3 shadow-lg md:absolute md:left-1/2 md:right-auto md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:flex md:flex-row md:items-center md:gap-7 md:border-0 md:bg-transparent md:p-0 md:shadow-none`}
            >
                <a
                    onClick={() => setMenuOpen(false)}
                    className="px-3 py-2 text-xs text-muted-foreground hover:text-brand-green"
                    href="#product"
                >
                    Product
                </a>
                <a
                    onClick={() => setMenuOpen(false)}
                    className="px-3 py-2 text-xs text-muted-foreground hover:text-brand-green"
                    href="#about"
                >
                    About
                </a>
                <div className="mt-1 flex items-center justify-between border-t border-border pt-3 md:hidden">
                    <Link className="px-3 text-xs font-semibold text-foreground/90" to="/login">
                        Sign in
                    </Link>
                    <Button to="/signup" size="small">
                        Get started <ArrowRight size={13} />
                    </Button>
                </div>
            </nav>
            <div className="flex items-center gap-3 sm:gap-5">
                <div className="hidden items-center gap-5 md:flex">
                    <Link
                        className="text-xs font-semibold text-foreground/90 hover:text-brand-green"
                        to="/login"
                    >
                        Sign in
                    </Link>
                    <Button to="/signup" size="small">
                        Get started <ArrowRight size={13} />
                    </Button>
                </div>
            </div>
        </header>
    );
}

function ProductPreview() {
    return (
        <div className="mx-auto w-full max-w-[590px] rounded-lg border border-border bg-card text-left shadow-lg">
            <div className="flex items-center justify-between border-b border-border px-5 py-3.5">
                <div className="flex items-center gap-2 text-xs font-semibold text-foreground/90">
                    <span className="grid size-7 place-items-center rounded-md bg-accent text-brand-leaf">
                        <Wallet size={14} />
                    </span>{" "}
                    Studio North
                </div>
                <span className="flex items-center gap-1.5 text-[10px] text-muted-foreground/90">
                    <i className="size-1.5 rounded-full bg-brand-leaf" /> Updated just now
                </span>
            </div>
            <div className="px-5 pb-3 pt-5 sm:px-7">
                <p className="text-xs text-muted-foreground/90">Total across accounts</p>
                <div className="mt-1 flex items-end justify-between">
                    <strong className="font-display text-3xl font-semibold tracking-[-.06em] text-foreground sm:text-4xl">
                        €48,290
                        <span className="text-xl font-medium text-muted-foreground/90">.00</span>
                    </strong>
                    <span className="mb-1 inline-flex items-center gap-1 rounded-full bg-accent px-2 py-1 text-[10px] font-semibold text-brand-leaf">
                        <ArrowUpRight size={12} /> 12.8%
                    </span>
                </div>
                <div className="mt-5 flex h-14 items-end gap-1 border-b border-border sm:h-16">
                    {[
                        29, 41, 34, 53, 46, 66, 52, 45, 79, 63, 86, 69, 95, 73, 88, 78, 96, 69, 83,
                        92, 73, 99,
                    ].map((height, index) => (
                        <span
                            key={index}
                            className={`flex-1 rounded-t-[2px] ${index === 16 ? "bg-brand-leaf" : "bg-accent"}`}
                            style={{ height: `${height}%` }}
                        />
                    ))}
                </div>
                <div className="mt-2 flex justify-between text-[9px] text-muted-foreground/70">
                    <span>Oct 01</span>
                    <span>Oct 08</span>
                    <span>Oct 15</span>
                    <span>Oct 22</span>
                    <span>Oct 31</span>
                </div>
            </div>
            <div className="border-t border-border px-5 pb-1 sm:px-7">
                <div className="flex items-center justify-between py-3">
                    <span className="text-[10px] font-semibold uppercase tracking-[.1em] text-muted-foreground/90">
                        Recent activity
                    </span>
                    <a className="text-[10px] font-semibold text-brand-leaf" href="#product">
                        View all <ArrowRight size={11} className="ml-0.5 inline" />
                    </a>
                </div>
                {transactions.map((transaction) => (
                    <div
                        key={transaction.merchant}
                        className="flex items-center gap-3 border-t border-border py-2.5"
                    >
                        <span
                            className={`grid size-7 place-items-center rounded-full text-[10px] font-bold ${transaction.mark}`}
                        >
                            {transaction.initials}
                        </span>
                        <span className="min-w-0 flex-1">
                            <strong className="block text-[11px] font-semibold text-foreground/90">
                                {transaction.merchant}
                            </strong>
                            <small className="mt-0.5 block text-[9px] text-muted-foreground/80">
                                {transaction.detail}
                            </small>
                        </span>
                        <strong
                            className={`text-[11px] font-medium ${transaction.amount.startsWith("+") ? "text-brand-leaf" : "text-foreground/80"}`}
                        >
                            {transaction.amount}
                        </strong>
                    </div>
                ))}
            </div>
        </div>
    );
}

function Feature({
    icon: Icon,
    title,
    description,
}: {
    icon: typeof Wallet;
    title: string;
    description: string;
}) {
    return (
        <article className="rounded-lg border border-border bg-card p-5 text-left">
            <span className="grid size-9 place-items-center rounded-md bg-secondary text-brand-leaf">
                <Icon size={17} />
            </span>
            <h3 className="mt-4 text-sm font-semibold text-foreground">{title}</h3>
            <p className="mt-2 text-xs leading-5 text-muted-foreground">{description}</p>
        </article>
    );
}

export default function LandingPage() {
    return (
        <main id="top" className="min-h-screen bg-card text-foreground">
            <Header />
            <section className="mx-auto grid max-w-7xl items-center gap-10 px-5 pb-16 pt-12 sm:px-8 md:grid-cols-[.88fr_1.12fr] md:gap-12 md:pb-20 md:pt-16">
                <div className="text-center md:text-left">
                    <span className="inline-flex items-center gap-2 rounded-full border border-border px-3 py-1.5 text-[10px] font-medium text-muted-foreground">
                        <Leaf size={12} /> Business finances in better balance
                    </span>
                    <h1 className="mx-auto mt-6 max-w-xl font-display text-4xl font-semibold leading-[1.08] tracking-[-.065em] text-foreground sm:text-5xl md:mx-0 md:text-[3.35rem] lg:text-6xl">
                        A clearer view of your business finances.
                    </h1>
                    <p className="mx-auto mt-5 max-w-md text-sm leading-6 text-muted-foreground sm:text-base md:mx-0">
                        Bring accounts, cards, and team spending together in one calm, easy-to-use
                        workspace.
                    </p>
                    <div className="mt-7 flex flex-wrap justify-center gap-3 md:justify-start">
                        <Button to="/signup">
                            Create your account <ArrowRight size={15} />
                        </Button>
                        <Button href="#product" variant="secondary">
                            See how Jexis works
                        </Button>
                    </div>
                    <p className="mt-4 text-[10px] text-muted-foreground/80">
                        Thoughtful tools for growing businesses.
                    </p>
                </div>
                <div className="mx-auto w-full max-w-[650px] rounded-xl bg-background p-3 sm:p-5">
                    <ProductPreview />
                    <div className="mt-4 flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-[10px] text-muted-foreground">
                        <span className="inline-flex items-center gap-1.5">
                            <Wallet size={12} className="text-brand-leaf" /> Connected accounts
                        </span>
                        <span className="hidden size-1 rounded-full bg-border sm:block" />
                        <span className="inline-flex items-center gap-1.5">
                            <CreditCard size={12} className="text-brand-leaf" /> Team cards
                        </span>
                        <span className="hidden size-1 rounded-full bg-border sm:block" />
                        <span className="inline-flex items-center gap-1.5">
                            <LockKeyhole size={12} className="text-brand-leaf" /> Secure by design
                        </span>
                    </div>
                </div>
            </section>
            <section
                id="product"
                className="border-y border-border bg-background px-5 py-14 sm:py-16"
            >
                <div className="mx-auto max-w-5xl">
                    <div className="mx-auto max-w-lg text-center">
                        <span className="text-[10px] font-semibold uppercase tracking-[.13em] text-brand-leaf">
                            MADE FOR THE EVERYDAY
                        </span>
                        <h2 className="mt-3 font-display text-2xl font-semibold tracking-[-.045em] text-foreground sm:text-3xl">
                            The essentials, all in one place.
                        </h2>
                        <p className="mt-3 text-xs leading-5 text-muted-foreground">
                            Get the context you need to make the next decision with confidence.
                        </p>
                    </div>
                    <div className="mt-8 grid gap-3 sm:grid-cols-3">
                        <Feature
                            icon={Wallet}
                            title="Accounts, together"
                            description="See balances and activity across your business accounts in one clear view."
                        />
                        <Feature
                            icon={CreditCard}
                            title="Team spending, in control"
                            description="Give your team the freedom to get things done, with cards and useful limits."
                        />
                        <Feature
                            icon={ArrowUpRight}
                            title="A better view of cash"
                            description="Understand what’s coming in and going out, then plan your next move."
                        />
                    </div>
                </div>
            </section>
            <section id="about" className="px-5 py-14 text-center sm:py-16">
                <p className="mx-auto max-w-xl font-display text-xl font-medium leading-relaxed tracking-[-.035em] text-foreground/90 sm:text-2xl">
                    Less time keeping up with money. More time getting on with work.
                </p>
                <Button to="/signup" className="mt-6">
                    Get started with Jexis <ArrowRight size={15} />
                </Button>
            </section>
            <footer className="border-t border-border px-5 py-5">
                <div className="mx-auto flex max-w-6xl flex-col items-center gap-3 sm:flex-row">
                    <Wordmark />
                    <span className="text-[10px] text-muted-foreground/90 sm:ml-2">
                        Business finances in better balance.
                    </span>
                    <div className="flex gap-5 text-[10px] text-muted-foreground sm:ml-auto">
                        <a href="#product">Product</a>
                        <a href="#about">About</a>
                        <Link to="/login">Sign in</Link>
                        <a href="mailto:hello@jexis.com">Contact</a>
                    </div>
                    <span className="text-[10px] text-muted-foreground/70">© 2026 Jexis</span>
                </div>
            </footer>
        </main>
    );
}
