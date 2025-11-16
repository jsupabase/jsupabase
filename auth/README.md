# 🔐 jsupabase-auth

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Authentication client for Supabase Auth (GoTrue) providing sign-in, session management, and auth events.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [API Reference](#api-reference)
- [Quick Start](#quick-start)
- [Best practices](#best-practices)
- [Error handling](#error-handling)
- [Troubleshooting](#troubleshooting)
- [Status & Roadmap](#status--roadmap)
- [Resources](#resources)
- [Related modules](#related-modules)
- [License](#license)
- [Endpoints used](#endpoints-used)

## Overview

`AuthClient` exposes a fluent async API to authenticate users against Supabase Auth (GoTrue). It returns `CompletableFuture` for all operations and integrates with the SDK so that subsequent calls use the fresh JWT automatically.

## Installation

```bash
./gradlew :auth:build
```

## Architecture

```text
+------------------+      auth()        +------------------+      builds paths      +----------------+
|  SupabaseClient  | -----------------> |   AuthClient     | --------------------> |   AuthPaths    |
+------------------+                    +------------------+                        +--------+-------+
                                                                                             |
                                                                                             | HTTP
                                                                                             v
                                                                                   HttpClientBase (core)
                                                                                             |
                                                                                             v
                                                                                   Supabase Auth (REST)
```

## Features

- Email/password sign-in and sign-up
- Sign-out
- Retrieve current session/JWT
- Token refresh
- Auth state change events (if configured)

## API Reference

```java
class AuthClient {
    CompletableFuture<AuthResponse> signInWithPassword(String email, String password);
    CompletableFuture<AuthResponse> signUp(String email, String password);
    CompletableFuture<Void>         signOut();

    CompletableFuture<Session>      getSession();
    CompletableFuture<AuthResponse> refreshToken();

    void onAuthStateChanged(java.util.function.Consumer<AuthChangeEvent> listener);
}
```

## Quick Start

```java
SupabaseConfig cfg = new SupabaseConfig.Builder(
    "https://YOUR-PROJECT.supabase.co",
    "YOUR-ANON-OR-SERVICE-KEY"
).build();
SupabaseClient supabase = SupabaseClient.create(cfg);

AuthClient auth = supabase.auth();
auth.signInWithPassword("user@example.com", "password")
    .thenAccept(res -> {
        System.out.println("User: " + res.getUser().getEmail());
        System.out.println("JWT:  " + res.getSession().getAccessToken());
    })
    .join();
```

## Best practices

- Store tokens securely; never hardcode keys in source code.
- Refresh tokens proactively based on expiry for long-lived apps.
- Always handle async failures in auth flows.

## Error handling

- Errors surface as `SupabaseException` with HTTP status and message when available.

## Troubleshooting

- 401 Unauthorized: invalid credentials or expired token
- 403 Forbidden: insufficient policies/roles
- Network errors: verify URL/key and connectivity

## Status & Roadmap

- Status: Stable for password-based flows.
- Roadmap: Expand OAuth/OTP coverage, event hooks, token persistence helpers.

## Resources

- Supabase Auth Docs: https://supabase.com/docs/guides/auth

## Related modules

- Core: [`../core/README.md`](../core/README.md)
- Client: [`../client/README.md`](../client/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

From `AuthPaths`:
- `POST /signup`
- `POST /token`
- `POST /sso/idtoken`
- `POST /verify`
- `POST /otp`
- `POST /oauth`
- `GET/PUT /user`
- `POST /logout`
- `POST /recover`
