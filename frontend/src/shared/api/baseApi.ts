import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { BaseQueryFn, FetchArgs, FetchBaseQueryError } from "@reduxjs/toolkit/query";
import { env } from "@/shared/config/env";
import { sessionEnded } from "./sessionEvents";

// A separate coordinator per store also keeps tests and future SSR requests isolated.
export function createBaseQuery(): BaseQueryFn<string | FetchArgs, unknown, FetchBaseQueryError> {
    const request = fetchBaseQuery({ baseUrl: env.apiUrl, credentials: "include", timeout: 15000 });
    const coordinators = new WeakMap<
        object,
        { refresh?: ReturnType<typeof request>; generation: number }
    >();

    return async (args, api, options) => {
        let coordinator = coordinators.get(api.dispatch);
        if (!coordinator) {
            coordinator = { generation: 0 };
            coordinators.set(api.dispatch, coordinator);
        }
        const url = typeof args === "string" ? args : args.url;
        const publicRequest = [
            "/auth/login",
            "/auth/signup",
            "/auth/forgot-password",
            "/auth/logout",
        ].includes(url);
        if (coordinator.refresh) await coordinator.refresh;
        const observedGeneration = coordinator.generation;
        let result = await request(args, api, options);
        if (result.error?.status !== 401 || publicRequest) return result;
        if (observedGeneration !== coordinator.generation) return request(args, api, options);
        if (!coordinator.refresh) {
            coordinator.refresh = request({ url: "/auth/refresh", method: "POST" }, api, options);
        }
        const pending = coordinator.refresh;
        const refreshed = await pending;
        if (coordinator.refresh === pending) {
            coordinator.refresh = undefined;
            coordinator.generation++;
            if (refreshed.error?.status === 401 || refreshed.error?.status === 403) {
                api.dispatch(sessionEnded());
            }
        }
        if (refreshed.error) return { error: refreshed.error };
        result = await request(args, api, options);
        if (result.error?.status === 401) api.dispatch(sessionEnded());
        return result;
    };
}

export const baseApi = createApi({
    reducerPath: "api",
    baseQuery: createBaseQuery(),
    endpoints: () => ({}),
});
