import { z } from "zod";

const payloadSchema = z.object({
    code: z.string().optional(),
    message: z.string().optional(),
    errors: z.array(z.object({ field: z.string(), message: z.string() })).optional(),
});

export interface ApiError {
    status?: number | string;
    code?: string;
    message: string;
    fields: { field: string; message: string }[];
}
export function getApiError(error: unknown): ApiError {
    const parsed = z
        .object({ status: z.union([z.number(), z.string()]), data: z.unknown().optional() })
        .safeParse(error);
    const payload = payloadSchema.safeParse(parsed.success ? parsed.data.data : undefined);
    const status = parsed.success ? parsed.data.status : undefined;
    return {
        status,
        code: payload.success ? payload.data.code : undefined,
        fields: payload.success ? (payload.data.errors ?? []) : [],
        message:
            payload.success && payload.data.message
                ? payload.data.message
                : status === 429
                  ? "Too many attempts. Please wait and try again."
                  : status === 401
                    ? "Your session has expired. Please sign in again."
                    : typeof status === "number"
                      ? "Something went wrong. Please try again."
                      : "Unable to connect. Please try again.",
    };
}
