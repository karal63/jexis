import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { AuthUser } from "../api/auth.types";
import { sessionEnded } from "@/shared/api/sessionEvents";
interface SessionState {
    status: "checking" | "authenticated" | "anonymous";
    user: AuthUser | null;
}
const initialState: SessionState = { status: "checking", user: null };
const sessionSlice = createSlice({
    name: "session",
    initialState,
    reducers: {
        sessionReceived(state, action: PayloadAction<AuthUser>) {
            state.user = action.payload;
            state.status = "authenticated";
        },
    },
    extraReducers: (builder) =>
        builder.addCase(sessionEnded, (state) => {
            state.user = null;
            state.status = "anonymous";
        }),
});
export const { sessionReceived } = sessionSlice.actions;
export default sessionSlice.reducer;
