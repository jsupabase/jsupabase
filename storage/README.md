# 📦 jsupabase-storage

![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue.svg) ![Java](https://img.shields.io/badge/java-11%2B-orange.svg)

Type-safe Java client for Supabase Storage with fluent APIs for buckets and objects, async I/O, signed/public URLs, and optional image transformations.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Architecture](#architecture)
- [Features](#features)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
  - [StorageClient](#storageclient)
  - [Bucket operations](#bucket-operations)
  - [Object operations](#object-operations)
  - [Image transformations](#image-transformations)
  - [DTOs](#dtos)
- [Best practices](#best-practices)
- [Error handling](#error-handling)
- [Troubleshooting](#troubleshooting)
- [Status & Roadmap](#status--roadmap)
- [Resources](#resources)
- [Related modules](#related-modules)
- [License](#license)
- [Endpoints used](#endpoints-used)

## Overview

This module provides a fluent, async-first API to manage Supabase Storage: create/list/delete buckets, upload/download/list/move/copy/delete objects, generate signed/public URLs, and build transformation URLs for images. It uses Java 11 HttpClient and shares configuration and auth through `SupabaseConfig`.

## Installation

Build from the repository root or just this module:

```bash
./gradlew :storage:build
```

Artifacts are generated under `storage/build/libs/`.

## Architecture

```text
+------------------+     storage()     +------------------+     sub-clients      +---------------------+
|  SupabaseClient  | ----------------> |  StorageClient   | -------------------> | Bucket/Object/Trans |
+------------------+                    +------------------+                      +----------+----------+
                                                                                           |
                                                                                           | builds paths
                                                                                           v
                                                                                   StoragePaths (endpoints)
                                                                                           |
                                                                                           | HTTP
                                                                                           v
                                                                                 HttpClientBase (core)
                                                                                           |
                                                                                           v
                                                                                 Supabase Storage REST
```

Notes:
- `StorageClient` is the gateway. It returns sub-clients: `BucketClient`, `ObjectClient`, and `TransformationClient`.
- `StoragePaths` is the single source of truth for REST paths.
- Authentication and HTTP stack are provided by the `core` module.

## Features

- Bucket lifecycle: create, list, get, empty, delete
- Object operations: upload, download, list, move, copy, delete, batch delete
- URLs: public URL generation and signed URLs for secure temporary access
- Async I/O with `CompletableFuture`
- MIME detection and metadata handling
- Optional image transformation URL builder

## Quick Start

```java
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

// 1) Build config and client
SupabaseConfig cfg = new SupabaseConfig.Builder(
    "https://YOUR-PROJECT.supabase.co",
    "YOUR-ANON-OR-SERVICE-KEY"
).build();
SupabaseClient supabase = SupabaseClient.create(cfg);

// 2) Create a bucket
CreateBucketOptions options = new CreateBucketOptions.Builder("avatars")
    .setPublic(true)
    .build();
supabase.storage().bucket().createBucket(options).join();

// 3) Upload a file
Path file = Paths.get("profile.jpg");
UploadResponse upload = supabase.storage().object("avatars")
    .upload("users/u123/profile.jpg", file, true)
    .join();
System.out.println("Uploaded: " + upload.getFullPath());

// 4) Generate a signed URL (1 hour)
SignedUrlResponse signed = supabase.storage().object("avatars")
    .createSignedUrl("users/u123/profile.jpg", 3600)
    .join();
System.out.println("Signed URL: " + signed.getSignedUrl());

// 5) Download
Path dest = Paths.get("downloaded-profile.jpg");
supabase.storage().object("avatars")
    .download("users/u123/profile.jpg", dest)
    .join();
```

## API Reference

### StorageClient

```java
class StorageClient {
    BucketClient bucket();
    ObjectClient object(String bucketId);
    TransformationClient transformation(String bucketId);
}
```

- `bucket()`: returns a `BucketClient` for bucket-level operations.
- `object(bucketId)`: returns an `ObjectClient` bound to a bucket for object operations.
- `transformation(bucketId)`: returns a `TransformationClient` for image transformation URLs.

### Bucket operations

```java
// Create bucket
CreateBucketOptions options = new CreateBucketOptions.Builder("my-bucket")
    .setPublic(true)
    .build();
supabase.storage().bucket().createBucket(options).join();

// List buckets
List<Bucket> buckets = supabase.storage().bucket().listBuckets().join();

// Get a bucket
Bucket bucket = supabase.storage().bucket().getBucket("my-bucket").join();

// Empty a bucket (irreversible)
supabase.storage().bucket().emptyBucket("my-bucket").join();

// Delete a bucket (irreversible)
supabase.storage().bucket().deleteBucket("my-bucket").join();
```

### Object operations

```java
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

ObjectClient objects = supabase.storage().object("my-bucket");

// Upload
Path file = Paths.get("docs/report.pdf");
UploadResponse up = objects.upload("reports/2024/report.pdf", file, false).join();

// Download
Path dest = Paths.get("downloads/report.pdf");
Path downloaded = objects.download("reports/2024/report.pdf", dest).join();

// List
List<FileObject> files = objects.list("reports/2024/").join();

// Move (atomic)
objects.move("reports/2024/report.pdf", "archive/2024/report.pdf").join();

// Copy
objects.copy("templates/invoice.pdf", "invoices/2024/invoice-001.pdf").join();

// Delete single
objects.delete("archive/2024/report.pdf").join();

// Delete batch
objects.deleteMultiple(List.of("tmp/a.txt", "tmp/b.txt")).join();

// Public URL (bucket must be public)
String publicUrl = objects.getPublicUrl("images/logo.png");

// Signed URL (seconds)
SignedUrlResponse s = objects.createSignedUrl("private/doc.pdf", 3600).join();
```

### Image transformations

```java
// Build a transformed public URL (no network call)
String url = supabase.storage()
    .transformation("images")
    .resize(320, 240)     // example API if implemented
    .quality(80)
    .format("webp")
    .getPublicUrl("photos/banner.jpg");
```

Notes:
- Transformations are URL-based; no upload/download happens at this step.
- Final behavior depends on Supabase Storage and CDN configuration.

### DTOs

- `Bucket` – bucket metadata (id, name, owner, isPublic, limits, timestamps)
- `FileObject` – object metadata (name, id, bucketId, metadata: mimetype, size, lastModified, etc.)
- `UploadResponse` – upload result (id, key, fullPath)
- `SignedUrlResponse` – signed URL data (signedUrl, path, expiresAt)
- `MessageResponse` – message payload for non-entity operations
- `CreateBucketOptions` – bucket creation configuration (public, size limits, allowed MIME types)

## Best practices

- Use clear hierarchical paths for objects: `users/{id}/documents/...`.
- Keep buckets private by default and share via signed URLs.
- Set appropriate size/type limits at bucket creation to prevent abuse.
- Prefer batch delete for cleanup jobs.
- Always handle async failures and log meaningful context.

## Error handling

- All operations return `CompletableFuture<?>` and surface `SupabaseException` on failure.
- Common cases:
  - 401/403: invalid key or insufficient policies
  - 404: bucket/object not found
  - 409: conflict (object exists when upsert=false)
  - 413/415: size exceeded or unsupported media type
- Use `.exceptionally(...)` or `.handle(...)` to manage errors gracefully.

## Troubleshooting

- 404 on download: verify bucket, exact path (case-sensitive), and existence via `list()`.
- Forbidden (403): check RLS policies and whether you used anon key vs service role.
- Slow transfers: confirm file sizes and avoid blocking joins in UI threads.
- Public URL not working: ensure bucket is public and path is correct.

## Status & Roadmap

- Status: Stable for core bucket/object operations, signed/public URLs.
- Roadmap: progress callbacks, streaming for large files, richer transformation helpers.

## Resources

- Supabase Storage Docs: https://supabase.com/docs/guides/storage
- HTTP Client (Java 11): https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html
- CompletableFuture: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html

## Related modules

- Core (HTTP/WS, config): [`../core/README.md`](../core/README.md)
- Client (facade): [`../client/README.md`](../client/README.md)
- PostgREST (DB): [`../postgrest/README.md`](../postgrest/README.md)
- Realtime (WS): [`../realtime/README.md`](../realtime/README.md)
- Auth (GoTrue): [`../auth/README.md`](../auth/README.md)

## License

See [LICENSE](../LICENSE).

## Endpoints used

The client builds REST endpoints via `StoragePaths`:

- Buckets
  - `GET/POST /bucket`
  - `GET/PUT/DELETE /bucket/{id}`
  - `POST /bucket/{id}/empty`
- Objects
  - `GET/POST/PUT/DELETE /object/{bucket}/{path}`
  - `DELETE /object/{bucket}` (batch)
  - `POST /object/list/{bucket}` and `POST /object/list-v2/{bucket}`
  - `POST /object/move`, `POST /object/copy`
  - Auth/public metadata: `GET /object/info/authenticated/{bucket}/{path}`, `GET /object/info/public/{bucket}/{path}`
- Signed/Public
  - `POST /object/sign/{bucket}/{path}` (single) and `POST /object/sign/{bucket}` (multiple)
  - `GET /object/public/{bucket}/{path}` (URL composition)

