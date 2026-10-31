import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/shared/lib/utils";

const buttonStyles = cva(
    "inline-flex min-h-11 items-center justify-center gap-2 rounded-md px-5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
    {
        variants: {
            variant: {
                primary: "bg-brand-green text-primary-foreground hover:bg-brand-green-hover",
                secondary: "border border-border bg-transparent text-foreground hover:bg-accent",
                inverse: "bg-secondary text-foreground hover:bg-card",
            },
            size: {
                default: "",
                small: "min-h-9 px-4 text-xs",
            },
        },
        defaultVariants: { variant: "primary", size: "default" },
    },
);

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
    VariantProps<typeof buttonStyles> & { href?: string; children: ReactNode };

export function Button({ href, variant, size, className, children, ...props }: ButtonProps) {
    const classes = cn(buttonStyles({ variant, size }), className);

    if (href) {
        return (
            <a className={classes} href={href}>
                {children}
            </a>
        );
    }

    return (
        <button className={classes} {...props}>
            {children}
        </button>
    );
}
