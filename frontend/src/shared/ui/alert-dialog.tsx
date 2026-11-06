import { Dialog } from "./dialog";
import { Button } from "./button";
type AlertDialogProps = { title: string; message: string; closeLabel: string; onClose: () => void };
export function AlertDialog({ title, message, closeLabel, onClose }: AlertDialogProps) {
    return (
        <Dialog title={title} description={message} onClose={onClose} alert>
            <div className="mt-6 flex justify-end">
                <Button onClick={onClose}>{closeLabel}</Button>
            </div>
        </Dialog>
    );
}
