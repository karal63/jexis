import { baseApi } from "@/shared/api/baseApi";
import { sessionReceived } from "@/features/auth/model/sessionSlice";
import type { AuthUser } from "@/features/auth/api/auth.types";
import type { RootState } from "@/app/store";
export interface UpdatePersonalInfoRequest {
    id: string;
    firstName: string;
    lastName: string;
    phoneNumber: string | null;
}
export const profileApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        updatePersonalInfo: builder.mutation<AuthUser, UpdatePersonalInfoRequest>({
            query: ({ id, ...body }) => ({ url: `/users/${id}/edit`, method: "PATCH", body }),
            async onQueryStarted(arg, { dispatch, getState, queryFulfilled }) {
                const generation = (getState() as RootState).session.generation;
                try {
                    const { data } = await queryFulfilled;
                    const session = (getState() as RootState).session;
                    if (
                        session.status === "authenticated" &&
                        session.user?.id === arg.id &&
                        session.generation === generation
                    )
                        dispatch(sessionReceived(data));
                } catch {
                    /* Form owns request errors. */
                }
            },
        }),
    }),
});
export const { useUpdatePersonalInfoMutation } = profileApi;
