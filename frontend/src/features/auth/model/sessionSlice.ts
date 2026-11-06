import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { AuthUser } from "../api/auth.types";
import { sessionEnded } from "@/shared/api/sessionEvents";
interface SessionState {
    status: "checking" | "authenticated" | "anonymous";
    user: AuthUser | null;
    generation: number;
}
const initialState: SessionState = { status: "checking", user: null, generation: 0 };
const sessionSlice = createSlice({
    name: "session",
    initialState,
    reducers: {
        sessionReceived(state, action: PayloadAction<AuthUser>) {
            if (state.user?.id !== action.payload.id) state.generation++;
            state.user = action.payload;
            state.status = "authenticated";
        },
    },
    extraReducers: (builder) =>
        builder.addCase(sessionEnded, (state) => {
            state.generation++;
            state.user = null;
            state.status = "anonymous";
        }),
});
export const { sessionReceived } = sessionSlice.actions;
export default sessionSlice.reducer;
