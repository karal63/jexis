import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/shared/lib/utils";

const buttonStyles = cva(
  "inline-flex min-h-11 items-center justify-center gap-2 rounded-md px-5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#92ac7f] focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        primary: "bg-[#294d34] text-white hover:bg-[#203e2a]",
        secondary: "border border-[#d8ded4] bg-transparent text-[#263d2c] hover:bg-[#eef1e9]",
        inverse: "bg-[#f4f4e9] text-[#243c2c] hover:bg-white",
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
    return <a className={classes} href={href}>{children}</a>;
  }

  return <button className={classes} {...props}>{children}</button>;
}
