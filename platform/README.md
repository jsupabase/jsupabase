# 🧩 jsupabase-platform

> ⚠️ Warning: This module is under active development and is not yet ready for use. Public APIs may change and functionality is incomplete.

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Cross-cutting platform helpers and environment detection utilities used by the jsupabase SDK.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [API Reference](#api-reference)
- [Quick Start](#quick-start)
- [Related modules](#related-modules)
- [License](#license)
- [Endpoints used](#endpoints-used)

## Overview

Provides utilities to detect runtime/platform characteristics and convenience helpers for other modules.

## Installation

```bash
./gradlew :platform:build
```

## Architecture

```text
+------------------+        uses        +-----------------------+
|  Module Clients  | -----------------> |  SupabasePlatform*    |
+------------------+                    +-----------------------+
```

## Features

- Detect runtime/platform traits (e.g., OS, environment)
- Helper methods used by higher-level clients

## API Reference

```java
class SupabasePlatformClient {
    String osName();
    boolean isWindows();
    boolean isUnix();
}
```

## Quick Start

```java
SupabasePlatformClient platform = new SupabasePlatformClient();
System.out.println("OS: " + platform.osName());
```

## Related modules

- Client: [`../client/README.md`](../client/README.md)
- Core: [`../core/README.md`](../core/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

This module does not call endpoints directly.
