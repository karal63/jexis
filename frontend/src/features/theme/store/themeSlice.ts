import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
export type Theme = "light" | "dark" | "system";
function readTheme(): Theme {
    try {
        const stored = localStorage.getItem("theme");
        if (stored === "light" || stored === "dark") return stored;
    } catch {
        /* Use system preference. */
    }
    return "system";
}
const themeSlice = createSlice({
    name: "theme",
    initialState: { theme: readTheme() },
    reducers: {
        setTheme(state, action: PayloadAction<Theme>) {
            state.theme = action.payload;
        },
    },
});
export const { setTheme } = themeSlice.actions;
export default themeSlice.reducer;
