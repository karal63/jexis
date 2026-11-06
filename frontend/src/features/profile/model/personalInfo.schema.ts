import { z } from "zod";
import type { Translator } from "@/shared/i18n/en";
import { createProfileFields } from "@/shared/forms/profileFields";
export const createPersonalInfoSchema = (t: Translator) => z.object(createProfileFields(t));
export type PersonalInfoValues = z.infer<ReturnType<typeof createPersonalInfoSchema>>;
