# 🗃️ jsupabase-postgrest

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Typed PostgREST client for database operations with fluent builders for `select`, `insert`, `update`, `delete`, and `rpc`.

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

Provides a fluent, async API to interact with Supabase PostgREST. Builders allow you to compose queries with filters and options.

## Installation

```bash
./gradlew :postgrest:build
```

## Architecture

```text
+------------------+    postgrest()    +-------------------+    query builders    +----------------+
|  SupabaseClient  | ----------------> |  PostgrestClient  | -------------------> |  builder/*     |
+------------------+                    +-------------------+                     +--------+-------+
                                                                                          |
                                                                                          | HTTP
                                                                                          v
                                                                                HttpClientBase (core)
                                                                                          |
                                                                                          v
                                                                                Supabase PostgREST
```

## Features

- Fluent builders for CRUD and RPC
- Type-safe filters and options
- Async-first API (CompletableFuture)

## API Reference

```java
class PostgrestClient {
    TableBuilder table(String tableName);
}

class TableBuilder {
    SelectBuilder select(String columns);
    InsertBuilder insert(Object payload);
    UpdateBuilder update(Object payload);
    DeleteBuilder delete();
}

class SelectBuilder {
    SelectBuilder eq(String col, Object val);
    SelectBuilder lt(String col, Object val);
    SelectBuilder order(String col, boolean ascending);
    SelectBuilder limit(int n);
    java.util.concurrent.CompletableFuture<java.util.List<java.util.Map<String,Object>>> execute();
}
```

## Quick Start

```java
import java.util.List;
import java.util.Map;

List<Map<String, Object>> rows = supabase.postgrest().table("todos")
    .select("id,title")
    .eq("user_id", userId)
    .order("created_at", true)
    .limit(10)
    .execute()
    .join();
```

## Best practices

- Keep column lists explicit in `select()` for smaller payloads.
- Combine filters (`eq`, `lt`, `like`) and pagination (`limit`) for efficient queries.
- Always handle async failures.

## Error handling

- Errors surface as `SupabaseException` with HTTP status and message when available.

## Troubleshooting

- 404 Not Found: wrong table or insufficient permissions
- 400 Bad Request: invalid payload or columns
- 403 Forbidden: RLS policy denied

## Status & Roadmap

- Status: Stable for standard CRUD.
- Roadmap: Expand RPC helpers, typed DTO mapping utilities.

## Resources

- PostgREST Docs: https://postgrest.org/en/stable/
- Supabase Database Docs: https://supabase.com/docs/guides/database

## Related modules

- Core: [`../core/README.md`](../core/README.md)
- Client: [`../client/README.md`](../client/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

- CRUD: `GET/POST/PATCH/DELETE /rest/v1/{table}` with query parameters for filters/order/limits
- RPC: `POST /rest/v1/rpc/{function}`
