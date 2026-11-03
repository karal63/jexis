import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { FormField, type FormFieldProps } from "./form-field";
export function PasswordInput(props: Omit<FormFieldProps, "type" | "trailing">) {
    const [visible, setVisible] = useState(false);
    return (
        <FormField
            {...props}
            type={visible ? "text" : "password"}
            trailing={
                <button
                    type="button"
                    onClick={() => setVisible((value) => !value)}
                    aria-label={`${visible ? "Hide" : "Show"} ${props.label.toLowerCase()}`}
                    aria-pressed={visible}
                    className="absolute inset-y-0 right-0 grid w-11 place-items-center text-muted-foreground focus-visible:outline-2 focus-visible:outline-ring"
                >
                    {visible ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
            }
        />
    );
}
