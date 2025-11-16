# 🧭 jsupabase-client

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Gateway entry point that wires all product modules (auth, postgrest, storage, realtime) behind a single `SupabaseClient`.

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

`SupabaseClient` centralizes configuration (`SupabaseConfig`) and exposes the product modules:
- `auth()` → AuthClient (GoTrue)
- `postgrest()` → PostgrestClient (database)
- `storage()` → StorageClient (object storage)
- `realtime()` → RealtimeClient (WebSocket connection + channels)

## Installation

Build from the repository root:

```bash
./gradlew :client:build
```

## Architecture

```text
+------------------+          exposes            +------------------+
|  SupabaseClient  | --------------------------> |  Module Clients  |
+------------------+                             +------------------+
         |                                                  |
         | shares SupabaseConfig + core infra               | use core infra
         v                                                  v
  SupabaseConfig (url, key, paths)            HttpClientBase / WebSocketClientBase (core)
```

## Features

- Single facade to access all product modules
- Shares configuration and auth across modules
- Lightweight construction (no network call on create)

## API Reference

```java
public final class SupabaseClient {
    static SupabaseClient create(SupabaseConfig config);

    AuthClient auth();
    PostgrestClient postgrest();
    StorageClient storage();
    RealtimeClient realtime();
}
```

## Quick Start

```java
import java.util.List;
import java.util.Map;

SupabaseConfig cfg = new SupabaseConfig.Builder(
    "https://YOUR-PROJECT.supabase.co",
    "YOUR-ANON-OR-SERVICE-KEY"
).build();
SupabaseClient supabase = SupabaseClient.create(cfg);

// Sign-in (optional)
supabase.auth().signInWithPassword("user@example.com", "password").join();

// List buckets
List<Bucket> buckets = supabase.storage().bucket().listBuckets().join();

// Select rows
List<Map<String, Object>> rows = supabase.postgrest().table("todos")
    .select("id,title")
    .limit(10)
    .execute()
    .join();
```

## Best practices

- Create a single `SupabaseClient` per application context and reuse it.
- Sign-in early if you will access private resources; the JWT will be propagated automatically.

## Error handling

- Async operations return `CompletableFuture<?>` and surface `SupabaseException` (from `core`).

## Troubleshooting

- Most issues originate from sub-clients (Auth, PostgREST, Storage, Realtime).
- Ensure `SupabaseConfig` URL/key are correct and network connectivity is available.

## Status & Roadmap

- Status: Stable facade used by all modules.
- Roadmap: Additional convenience factory methods and configuration helpers.

## Resources

- Supabase Docs: https://supabase.com/docs
- Java 11 HttpClient: https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html

## Related modules

- Core: [`../core/README.md`](../core/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

This module does not call endpoints directly. It delegates operations to product clients.
