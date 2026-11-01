import { Link, type LinkProps } from "react-router-dom";
import type { AnchorHTMLAttributes } from "react";
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

type StyleProps = VariantProps<typeof buttonStyles> & { children: ReactNode; className?: string };
type ButtonProps = StyleProps &
    (
        | ({ to: LinkProps["to"]; href?: never } & Omit<LinkProps, "to" | "children">)
        | ({ href: string; to?: never } & AnchorHTMLAttributes<HTMLAnchorElement>)
        | ({ href?: never; to?: never } & ButtonHTMLAttributes<HTMLButtonElement>)
    );
export function Button(props: ButtonProps) {
    const { variant, size, className, children, ...rest } = props;
    const classes = cn(buttonStyles({ variant, size }), className);
    if ("to" in rest && rest.to !== undefined) {
        const { href: _href, ...linkProps } = rest as LinkProps & { href?: never };
        return (
            <Link {...linkProps} className={classes}>
                {children}
            </Link>
        );
    }
    if ("href" in rest && rest.href !== undefined) {
        const { to: _to, ...anchorProps } = rest as AnchorHTMLAttributes<HTMLAnchorElement> & {
            href: string;
            to?: never;
        };
        return (
            <a {...anchorProps} className={classes}>
                {children}
            </a>
        );
    }
    return (
        <button
            type="button"
            {...(rest as ButtonHTMLAttributes<HTMLButtonElement>)}
            className={classes}
        >
            {children}
        </button>
    );
}
