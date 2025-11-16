# ⚡ jsupabase-realtime

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Reliable Phoenix Channels client for Supabase Realtime (Postgres changes, Broadcast, Presence) with a singleton WebSocket, channel-based subscriptions, and typed filters.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
  - [RealtimeClient](#realtimeclient)
  - [RealtimeChannelBuilder](#realtimechannelbuilder)
  - [DTOs (filters)](#dtos-filters)
  - [Topics & Events](#topics--events)
- [Best practices](#best-practices)
- [Error handling](#error-handling)
- [Troubleshooting](#troubleshooting)
- [Status & Roadmap](#status--roadmap)
- [Resources](#resources)
- [Related modules](#related-modules)
- [License](#license)
- [Endpoints used](#endpoints-used)

## Overview

Provides a singleton WebSocket connection and a channel builder for topic-based subscriptions, following Phoenix Channels semantics and Supabase Realtime conventions.

## Installation

```bash
./gradlew :realtime:build
```

## Architecture

```text
+------------------+   realtime()    +--------------------+    WS lifecycle     +-----------------------+
|  SupabaseClient  | --------------> |  RealtimeClient    | ------------------> | WebSocketClientBase   |
+------------------+                 +---------+----------+                     +-----------+-----------+
                                              |                                          |
                                              | channels per topic                       | sends/receives
                                              v                                          v
                                     RealtimeChannelBuilder                     Supabase Realtime (WS)
```

## Features

- Singleton WS connection with heartbeat and reconnect backoff
- Channel-per-topic model with JOIN/LEAVE
- Postgres changes subscriptions via typed filters
- Broadcast and Presence utilities
- Ref correlation to match JOIN requests with replies (phx_reply)

## API Reference

```java
class RealtimeClient {
    RealtimeChannelBuilder channel(String topic);
}

class RealtimeChannelBuilder {
    RealtimeChannelBuilder onPostgresChanges(PostgresChangesFilter filter);
    RealtimeChannelBuilder onBroadcast(BroadcastEventFilter filter);
    RealtimeChannelBuilder onPresence(PresenceEventFilter filter);

    RealtimeChannelBuilder subscribe(java.util.function.Consumer<String> statusCallback);
    void unsubscribe();

    void send(String event, java.util.Map<String, Object> payload);
}
```

## Quick Start

```java
import java.util.Map;

PostgresChangesFilter filter = new PostgresChangesFilter.Builder(RealtimeEvent.ALL)
    .schema("public")
    .table("todos")
    .callback(payload -> System.out.println("Change: " + payload))
    .build();

RealtimeChannelBuilder channel = supabase.realtime()
    .channel("realtime:public:todos")
    .onPostgresChanges(filter)
    .subscribe(status -> System.out.println("Realtime status=" + status));

channel.send("custom_event", Map.of("msg", "hi"));

channel.unsubscribe();
```

## Best practices

- Keep a single `RealtimeClient` instance; reuse it application-wide.
- Reuse channels per topic instead of re-creating frequently.
- Use precise Postgres filters (schema/table/where) to reduce noise.
- Treat reconnection events idempotently; reattach listeners as needed.

## Error handling

- Connection and message errors are logged via SLF4J.
- Protocol/JSON errors surface as `SupabaseException` (or module-specific subtypes if present).
- JOIN reply handling uses ref matching to avoid race conditions on reconnects.

## Troubleshooting

- `ConnectException` / `UnresolvedAddressException`: network/DNS/firewall issues.
- JOIN timeout: invalid topic, connectivity issues, or auth problems.
- No events received: verify schema/table, RLS policies, and that Realtime is enabled for the DB.

## Status & Roadmap

- Status: Stable for basic channels and `postgres_changes`.
- Roadmap: richer presence helpers, backpressure strategies, structured broadcast payload helpers.

## Resources

- Supabase Realtime Docs: https://supabase.com/docs/guides/realtime
- Phoenix Channels: https://hexdocs.pm/phoenix/channels.html

## Related modules

- Core: [`../core/README.md`](../core/README.md)
- Client: [`../client/README.md`](../client/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

- WebSocket: `GET wss://{project}.supabase.co/realtime/v1/websocket?apikey={KEY}&vsn=2.0.0[&token={JWT}]`
- Phoenix events: `phx_join`, `phx_reply`, `phx_leave`, `heartbeat`
- Realtime events: `postgres_changes`, `broadcast`, `presence`
- Topics: `realtime:{schema}:{table}`
