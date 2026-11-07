import type { ReactNode } from "react";
import { ArrowLeft, Leaf } from "lucide-react";
import { Link } from "react-router-dom";

export function CenteredPage({
    title,
    description,
    backTo,
    backLabel,
    children,
}: {
    title: string;
    description: string;
    backTo: string;
    backLabel: string;
    children: ReactNode;
}) {
    return (
        <main className="flex min-h-screen flex-col items-center bg-card px-5 pb-8 text-foreground">
            <header className="relative flex w-full max-w-5xl items-center justify-center pt-[6vh]">
                <Link
                    to={backTo}
                    className="inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-.08em] text-foreground"
                >
                    <span className="grid size-7 place-items-center text-foreground">
                        <Leaf size={18} strokeWidth={2.5} />
                    </span>
                    jexis
                </Link>
            </header>
            <section className="mx-auto mt-[clamp(56px,11vh,128px)] w-full max-w-[430px] pb-10">
                <div className="mb-8 text-center">
                    <h1 className="font-display text-[27px] font-bold tracking-[-.045em] text-foreground">
                        {title}
                    </h1>
                    <p className="mt-2 text-sm text-muted-foreground">{description}</p>
                </div>
                {children}
            </section>
            <Link
                to={backTo}
                className="mt-auto inline-flex items-center gap-1.5 text-[11px] text-muted-foreground hover:text-foreground"
            >
                <ArrowLeft size={13} /> {backLabel}
            </Link>
        </main>
    );
}
