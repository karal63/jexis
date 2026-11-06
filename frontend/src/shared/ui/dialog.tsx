import * as Primitive from "@radix-ui/react-dialog";
import { useRef, type ReactNode } from "react";

// Mount while open. Capture the opener because this dialog need not have a Trigger.
export function Dialog({
    title,
    description,
    onClose,
    children,
    alert = false,
}: {
    title: string;
    description: string;
    onClose: () => void;
    children: ReactNode;
    alert?: boolean;
}) {
    const opener = useRef(
        document.activeElement instanceof HTMLElement ? document.activeElement : null,
    );
    return (
        <Primitive.Root
            open
            onOpenChange={(open) => {
                if (!open) onClose();
            }}
        >
            <Primitive.Portal>
                <Primitive.Overlay className="fixed inset-0 z-50 bg-black/50" />
                <Primitive.Content
                    role={alert ? "alertdialog" : "dialog"}
                    onCloseAutoFocus={(event) => {
                        event.preventDefault();
                        if (opener.current?.isConnected) opener.current.focus();
                    }}
                    className="fixed left-1/2 top-1/2 z-50 w-[calc(100%_-_2.5rem)] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-xl border border-border bg-card p-6 text-foreground shadow-xl"
                >
                    <Primitive.Title className="font-display text-xl font-bold">
                        {title}
                    </Primitive.Title>
                    <Primitive.Description className="mt-3 text-sm leading-6 text-muted-foreground">
                        {description}
                    </Primitive.Description>
                    {children}
                </Primitive.Content>
            </Primitive.Portal>
        </Primitive.Root>
    );
}
