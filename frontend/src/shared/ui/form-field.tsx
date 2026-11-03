import { useId, type InputHTMLAttributes, type ReactNode } from "react";
import { cn } from "@/shared/lib/utils";
export type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
    label: string;
    error?: string;
    hint?: string;
    trailing?: ReactNode;
};
export function FormField({
    label,
    error,
    hint,
    trailing,
    id,
    className,
    ...props
}: FormFieldProps) {
    const generatedId = useId();
    const inputId = id ?? generatedId;
    const description =
        [props["aria-describedby"], error || hint ? `${inputId}-description` : undefined]
            .filter(Boolean)
            .join(" ") || undefined;
    return (
        <div>
            <label
                htmlFor={inputId}
                className="mb-1.5 block text-xs font-semibold text-foreground/90"
            >
                {label}
            </label>
            <div className="relative">
                <input
                    {...props}
                    id={inputId}
                    aria-invalid={Boolean(error)}
                    aria-describedby={description}
                    className={cn(
                        "h-11 w-full rounded-md border border-input bg-card px-3.5 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-ring focus:ring-2 focus:ring-ring/15",
                        trailing && "pr-11",
                        error && "border-danger",
                        className,
                    )}
                />
                {trailing}
            </div>
            <p
                id={`${inputId}-description`}
                role={error ? "alert" : undefined}
                className={cn(
                    "mt-1 min-h-4 text-xs",
                    error ? "text-danger" : "text-muted-foreground",
                )}
            >
                {error || hint || " "}
            </p>
        </div>
    );
}
