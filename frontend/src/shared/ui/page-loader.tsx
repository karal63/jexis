import { useId } from "react";
import { Leaf } from "lucide-react";

export function PageLoader({ label }: { label: string }) {
    const gradientId = useId();

    return (
        <main className="grid min-h-screen place-items-center bg-background p-6">
            <div role="status" className="grid place-items-center">
                <Leaf
                    aria-hidden="true"
                    size={72}
                    strokeWidth={1.6}
                    color={`url(#${gradientId})`}
                    className="animate-pulse motion-reduce:animate-none"
                >
                    <defs>
                        <linearGradient id={gradientId} x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#555b58" />
                            <stop offset="50%" stopColor="#c8ceca" />
                            <stop offset="100%" stopColor="#676e69" />
                        </linearGradient>
                    </defs>
                </Leaf>
                <span className="sr-only">{label}</span>
            </div>
        </main>
    );
}
