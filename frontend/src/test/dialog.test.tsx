import { useState } from "react";
import { describe, expect, it } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Dialog } from "@/shared/ui/dialog";
function Example() {
    const [open, setOpen] = useState(false);
    return (
        <>
            <button onClick={() => setOpen(true)}>Open</button>
            {open && (
                <Dialog title="Example" description="Details" onClose={() => setOpen(false)}>
                    <a href="#first">First</a>
                    <button>Second</button>
                </Dialog>
            )}
        </>
    );
}
describe("shared dialog", () => {
    it("contains focus across multiple controls and restores the opener", async () => {
        render(<Example />);
        const browser = userEvent.setup();
        const opener = screen.getByRole("button", { name: "Open" });
        await browser.click(opener);
        const first = screen.getByRole("link", { name: "First" });
        await waitFor(() => expect(screen.getByRole("button", { name: "Second" })).toHaveFocus());
        first.focus();
        expect(opener.closest('[aria-hidden="true"]')).not.toBeNull();
        await browser.tab();
        expect(screen.getByRole("button", { name: "Second" })).toHaveFocus();
        await browser.tab();
        expect(first).toHaveFocus();
        await browser.tab({ shift: true });
        expect(screen.getByRole("button", { name: "Second" })).toHaveFocus();
        await browser.keyboard("{Escape}");
        await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
        expect(opener).toHaveFocus();
    });
});
