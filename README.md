# 🚀 jsupabase: Java SDK for Supabase (JDK 11+)

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)
![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

A Java SDK for Supabase, built on Java 11 native APIs (HttpClient / WebSocket) for async I/O, minimal external deps, and high performance.

---

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [Typical calls (per module)](#typical-calls-per-module)
  - [Auth (GoTrue)](#auth-gotrue)
  - [PostgREST (DB)](#postgrest-db)
  - [Storage (files)](#storage-files)
  - [Realtime (WebSocket)](#realtime-websocket)
- [Quick Start (end-to-end)](#quick-start-end-to-end)
- [Modules (summary + links)](#modules-summary--links)
  - [Core](core/README.md)
  - [Client](client/README.md)
  - [Auth](auth/README.md)
  - [PostgREST](postgrest/README.md)
  - [Storage](storage/README.md)
  - [Realtime](realtime/README.md)
  - [Platform](platform/README.md)
- [Configuration & logging](#configuration--logging)
- [Status & roadmap](#status--roadmap)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)
- [License](#license)

---

## Overview

jsupabase provides Java clients for Supabase services:
- Authentication (GoTrue)
- Database (PostgREST)
- Storage (object storage)
- Realtime (Phoenix Channels)

---

## Installation

This is a Gradle multi-module project. Requirements: Java 11+

```bash
# Clone and build everything
git clone https://github.com/your-org/jsupabase.git
cd jsupabase

# Full build (skip tests)
./gradlew build -x test

# Build a single module
./gradlew :storage:build
```

Use locally in other projects (optional):

```bash
# Publish to your local Maven repository
./gradlew publishToMavenLocal
```

> Artifacts will be available under `~/.m2/repository/io/github/jsupabase/*`.

---

## Architecture

```
jsupabase/
├── core/       (shared infra: HttpClientBase, WebSocketClientBase, SupabaseConfig, JsonUtil, exceptions)
├── client/     (facade: SupabaseClient)
├── auth/       (GoTrue: login, session, OAuth, OTP, events)
├── postgrest/  (PostgREST: select/insert/update/delete/rpc)
├── storage/    (Storage: buckets, objects, transformations)
├── realtime/   (Realtime: WS connection, channels, postgres/broadcast/presence)
├── platform/   (platform helpers)
```

Global diagram (actual flow):

```text
+------------------+      storage()/auth()/postgrest()/realtime()      +------------------+
|  SupabaseClient  | -------------------------------------------------> |   Module Client  |
+------------------+                                                    +---------+--------+
                                                                                |
                                                                                | builds paths
                                                                                v
                                                                  Paths (Storage/Auth/...)
                                                                                |
                                                                                | HTTP/WS request
                                                                                v
                                                 HttpClientBase / WebSocketClientBase (core)
                                                                                |
                                                                                v
                                                       Supabase REST APIs / Realtime WS
```

Key notes:
- All modules share `SupabaseConfig` (URL, keys, service paths) and core utilities.
- `client` exposes a single facade and reconfigures JWT after `signIn`.

---

## Features

- Async by default (`CompletableFuture`)
- Fluent, type-safe APIs across modules
- Lightweight deps (Jackson + SLF4J)
- Modular, extensible, maintainable architecture

---

## Typical calls (per module)

### Auth (GoTrue)
```
SupabaseClient.auth() → AuthClient → AuthPaths → HttpClientBase → Supabase Auth REST
```

### PostgREST (DB)
```
SupabaseClient.postgrest() → PostgrestClient → builders (select/insert/...) → HttpClientBase → PostgREST
```

### Storage (files)
```
SupabaseClient.storage() → StorageClient → BucketClient / ObjectClient / TransformationClient
                        → StoragePaths → HttpClientBase → Supabase Storage REST
```

### Realtime (WebSocket)
```
SupabaseClient.realtime() → RealtimeClient (singleton) → RealtimeConnection (WebSocket, heartbeats, refs)
                         → RealtimeChannelBuilder (per topic) → Phoenix events (phx_join/reply/...)
```

---

## Quick Start (end-to-end)

```java
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

// 1) Config and client
SupabaseConfig cfg = new SupabaseConfig.Builder(
    "https://YOUR-PROJECT.supabase.co",
    "YOUR-ANON-OR-SERVICE-KEY"
).build();
SupabaseClient supabase = SupabaseClient.create(cfg);

// 2) Auth (optional) – prepares JWT for the rest
supabase.auth().signInWithPassword("user@example.com", "password").join();

// 3) PostgREST – simple read
List<Map<String, Object>> rows = supabase.postgrest().table("todos")
    .select("id,title")
    .limit(10)
    .execute()
    .join();

// 4) Storage – create bucket and upload a file
CreateBucketOptions options = new CreateBucketOptions.Builder("avatars")
    .setPublic(true)
    .build();
supabase.storage().bucket().createBucket(options).join();

Path file = Paths.get("avatar.jpg");
supabase.storage().object("avatars")
    .upload("u123/avatar.jpg", file, true)
    .join();

// 5) Realtime – basic subscription (postgres_changes)
PostgresChangesFilter filter = new PostgresChangesFilter.Builder(RealtimeEvent.ALL)
    .schema("public")
    .table("todos")
    .callback(payload -> System.out.println("Change: " + payload))
    .build();

supabase.realtime()
    .channel("realtime:public:todos")
    .onPostgresChanges(filter)
    .subscribe(status -> System.out.println("Realtime status=" + status));
```

> Note: this example assumes working network connectivity and a valid Supabase project config.

---

## Modules (summary + links)

- core → shared infra (HttpClientBase, WebSocketClientBase, SupabaseConfig, JsonUtil, exceptions). See [`core/README.md`](core/README.md)
- client → `SupabaseClient` facade, reorchestrates JWT and exposes modules. See [`client/README.md`](client/README.md)
- auth → GoTrue: password, OTP, OAuth, sessions, events. See [`auth/README.md`](auth/README.md)
- postgrest → typed queries (select/insert/update/delete/rpc). See [`postgrest/README.md`](postgrest/README.md)
- storage → buckets, objects, public/signed URLs, transformations. See [`storage/README.md`](storage/README.md)
- realtime → WS connection, channels, postgres_changes/broadcast/presence. See [`realtime/README.md`](realtime/README.md)
- platform → platform helpers. See [`platform/README.md`](platform/README.md)

---

## Configuration & logging

- Config: `SupabaseConfig.Builder(supabaseUrl, supabaseKey)`
  - Internal modules derive service paths from `SupabaseConfig`.
- Logging: SLF4J API (`org.slf4j:slf4j-api`), default implementation: Logback (runtimeOnly)
  - Adjust levels via a `logback.xml` on your classpath.

Base dependencies (managed in root Gradle for subprojects):
- SLF4J 2.0.13 (API)
- Jackson Databind 2.17.1 (JSON)
- Logback 1.4.14 (runtime)

---

## Status & roadmap

Current status (0.1.0-SNAPSHOT):
- ✅ core, client, auth, postgrest, storage, realtime – operational

Next steps:
- Publish to Maven Central
- Advanced retries/timeouts per operation
- Progress & streaming for large files (Storage)
- Metrics/telemetry and more tests

---

## Troubleshooting

- DNS / Connectivity: `UnresolvedAddressException` / `ConnectException`
  - Verify your `SUPABASE_URL`, firewall/proxy and Internet connection.
- Java 11+: ensure you run a JDK 11 or newer.
- Gradle Wrapper: use `./gradlew` to avoid local incompatibilities.

---

## Resources

- Supabase Docs: https://supabase.com/docs
- Auth (GoTrue): https://supabase.com/docs/guides/auth
- PostgREST: https://postgrest.org/en/stable/
- Storage: https://supabase.com/docs/guides/storage
- Realtime: https://supabase.com/docs/guides/realtime

---

## License

See [LICENSE](LICENSE) at the project root.
