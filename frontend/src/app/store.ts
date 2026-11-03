import { combineReducers, configureStore, type UnknownAction } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import themeReducer from "@/features/theme";
import sessionReducer from "@/features/auth/model/sessionSlice";
import { baseApi } from "@/shared/api/baseApi";
import { sessionEnded } from "@/shared/api/sessionEvents";

const reducer = combineReducers({
    theme: themeReducer,
    session: sessionReducer,
    [baseApi.reducerPath]: baseApi.reducer,
});

export function createAppStore() {
    return configureStore({
        reducer: (state: ReturnType<typeof reducer> | undefined, action: UnknownAction) => {
            // Remove every cached user resource when a session ends.
            if (state && sessionEnded.match(action))
                state = { ...state, api: baseApi.reducer(undefined, { type: "@@init" }) };
            return reducer(state, action);
        },
        middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware),
    });
}

export const store = createAppStore();
setupListeners(store.dispatch);
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
