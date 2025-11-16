# 🧱 jsupabase-core

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Core layer powering all modules in the jsupabase SDK: HTTP/WS foundations, shared configuration, JSON utilities, and base exceptions.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [API Reference](#api-reference)
- [Quick Start](#quick-start)
- [Best practices](#best-practices)
- [Error handling](#error-handling)
- [Logging](#logging)
- [Dependencies](#dependencies)
- [Status & Roadmap](#status--roadmap)
- [Resources](#resources)
- [Related modules](#related-modules)
- [License](#license)
- [Endpoints used](#endpoints-used)

## Overview

The `core` module contains the shared infrastructure used by all product clients (auth, postgrest, storage, realtime). It standardizes how requests are built, authenticated, logged, deserialized, and how WebSocket connections are managed.

## Installation

Build from the repository root:

```bash
./gradlew :core:build
```

## Architecture

```text
+------------------+      builds requests       +-----------------------+
|  SupabaseConfig  | -------------------------> |  HttpClientBase       |
+------------------+                            +-----------------------+
          ^                                               |
          | shared by all modules                         | HTTP/JSON
          |                                               v
          |                                      Supabase REST APIs
          |                                                
          |   builds WS URI / manages state      +-----------------------+
          +------------------------------------> |  WebSocketClientBase  |
                                                 +-----------------------+
                                                          |
                                                          | WS (Phoenix/Realtime)
                                                          v
                                                  Supabase Realtime WS
```

## Features

- Async HTTP via Java 11 HttpClient
- WebSocket lifecycle with heartbeats and reconnect backoff
- Centralized auth headers (apikey, Bearer JWT)
- Jackson-based JSON (de)serialization
- Base exception model (`SupabaseException`)

## API Reference

- `HttpClientBase` – async HTTP ops + JSON mapping + error propagation
- `WebSocketClientBase` – connect/disconnect, heartbeat scheduler, reconnect
- `SupabaseConfig` – base URL, API key/JWT, per-service paths
- `JsonUtil` – DTO (de)serialization helpers
- `SupabaseException` – base runtime exception

## Quick Start

```java
SupabaseConfig config = new SupabaseConfig.Builder(
    "https://YOUR-PROJECT.supabase.co",
    "YOUR-ANON-OR-SERVICE-KEY"
).build();
SupabaseClient supabase = SupabaseClient.create(config);
// Then use: supabase.auth(), supabase.postgrest(), supabase.storage(), supabase.realtime()
```

## Best practices

- Share one `SupabaseConfig` across all module clients.
- Avoid `System.out/err`; use SLF4J with levels.
- Handle async failures using `.exceptionally(...)` or `.handle(...)`.

## Error handling

- IO and HTTP failures are surfaced as `SupabaseException` with status/message when available.
- WebSocket failures trigger reconnects according to the backoff policy in `WebSocketClientBase`.

## Logging

- SLF4J facade. Provide your backend (Logback included for dev/test in this repo).
- Avoid `System.out/err` in production code.

## Dependencies

Managed at the root Gradle level for all subprojects:
- SLF4J API: `org.slf4j:slf4j-api:2.0.13`
- Jackson Databind: `com.fasterxml.jackson.core:jackson-databind:2.17.1`
- Logback (runtime/test): `ch.qos.logback:logback-classic:1.4.14`
- JUnit Jupiter: `org.junit.jupiter:junit-jupiter:5.10.2`

## Status & Roadmap

- Status: Stable internal API used by all modules.
- Roadmap: Extended metrics/telemetry, richer retry strategies.

## Resources

- Java 11 HttpClient: https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html
- CompletableFuture: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html

## Related modules

- Client: [`../client/README.md`](../client/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

This module does not call endpoints directly. It provides HTTP/WebSocket foundations used by other modules.
