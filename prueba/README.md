# jsupabase-prueba (integration playground)

End-to-end integration playground that exercises all SDK modules (auth, postgrest, storage, realtime).

## Table of Contents

- [Overview](#overview)
- [Flow covered](#flow-covered)
- [How to run](#how-to-run)
- [Notes](#notes)
- [Related modules](#related-modules)

## Overview

This module provides a self-contained `main()` to demo and validate the integration across modules using a real Supabase project.

## Flow covered

1. Auth: email/password sign-in
2. PostgREST: basic CRUD
3. Realtime: subscribe to table changes
4. Storage: create bucket and upload a file

## How to run

From your IDE, run the `SuperAllModulesMain` main class.

Or via Gradle task (if configured):

```bash
./gradlew :prueba:run
```

## Notes

- Requires working Internet and a valid Supabase project URL and key.
- Logs are routed via SLF4J (Logback by default in this repo).

## Related modules

- Client (facade): [`../client/README.md`](../client/README.md)
- Core (HTTP/WS, config, exceptions): [`../core/README.md`](../core/README.md)
- Auth: [`../auth/README.md`](../auth/README.md)
- PostgREST: [`../postgrest/README.md`](../postgrest/README.md)
- Storage: [`../storage/README.md`](../storage/README.md)
- Realtime: [`../realtime/README.md`](../realtime/README.md)
