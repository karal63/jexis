# Jexis frontend

React, TypeScript, Vite, Tailwind, React Router, Redux Toolkit, RTK Query, React Hook Form, and Zod.

## Development

Use Node.js 22 or newer. Run `npm ci`, then `npm run dev` from this directory. Start the Spring Boot backend on port 3000; Vite proxies `/api` to it. Copy `.env.example` to `.env.local` to override `VITE_API_URL`. Vite environment variables are public browser configuration, so never put secrets in them.

Run `npm run build`, `npm run lint`, `npm run format:check`, and `npm test` before pushing. `npm run test:watch` runs tests while editing. The lockfile is committed and frontend checks also run in CI.

## Structure and ownership

- `app`: providers, routing, store composition, and typed Redux hooks.
- `pages`: route screens and navigation decisions after user actions.
- `features/auth`: authentication endpoints, DTOs, validation, session state, and forms. `index.ts` is the public interface for UI consumers.
- `shared/api`: the common RTK Query API, transport behavior, session events, and normalized errors.
- `shared/ui`: domain-independent controls with labels, errors, focus behavior, and consistent styling.
- `shared/config`: environment configuration.

Dependencies flow from app/pages to features to shared. Features must not import pages or app. Keep page-only components beside their page until reuse justifies extracting them. Do not add repositories or service classes around RTK Query by default.

Use local state for visual interactions, React Hook Form for forms, RTK Query for server resources, URL search parameters for shareable filters/pagination, and Redux slices for shared client state. The session slice owns the current authenticated identity; the current-user query is used to bootstrap it. Do not copy ordinary query results into Redux slices.

## API and session contract

The common API uses `fetchBaseQuery` with cookies and a 15-second timeout. Add feature endpoints with `baseApi.injectEndpoints`; use one API cache per backend. Define request/response types at the feature boundary. Normalize failures with `getApiError`; use `applyFormError` to map known backend field names to React Hook Form errors.

`GET /auth/me` restores the current user. On a protected request's 401, the transport calls `POST /auth/refresh` once and retries the original request once. Concurrent requests share an in-flight refresh within the store. Login, signup, forgot-password, and logout failures never trigger refresh. A rejected refresh ends the session and clears user caches; network/server errors allow retry without declaring the user logged out. Tokens remain in HttpOnly cookies, never localStorage or Redux.

Successful login/signup opens `/app`. Auth screens redirect authenticated users there. `/app` requires a session. Logout calls the server first; only success clears local identity and cached resources. A failed logout stays visible for retry. Backend authorization remains mandatory regardless of route guards.

Deploy the frontend and `/api` under the same HTTPS origin where possible. The backend currently issues Secure, SameSite=Strict cookies. Different-origin hosting requires deliberate backend cookie/CORS configuration; `credentials: include` alone cannot override browser cookie rules. The development proxy does not exist in a production Vite build: configure your reverse proxy to forward `/api` and serve `index.html` for frontend route navigation.

## UI conventions

Use `FormField` and `PasswordInput` for fields. They connect labels, invalid state, and description/error text with `aria-describedby`. Forms use `noValidate` so Zod/RHF supplies one consistent validation experience; the backend still enforces all rules. Keep password visibility local to each input.

Use `Button to="/path"` for internal navigation, `Button href="https://..."` for external links, and `Button onClick={...}` for actions. Appearance uses semantic CSS variables; theme preference supports light, dark, and system and is persisted independently of session data.

The tests exercise form behavior and the API/session lifecycle using mocked HTTP responses. Backend auth integration tests separately cover the current-user endpoint and session enforcement. They do not replace deployment/browser checks against a running backend.
