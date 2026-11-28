# Sync API (Milestone 2)

## Authentication
All endpoints require `Authorization: Bearer <Firebase ID token>`. The application user is derived from the verified Firebase UID; request-supplied user IDs are ignored.

## POST /api/sync/push

```json
{
  "mutations": [
    {
      "mutationId": "uuid",
      "entityType": "WORKOUT",
      "syncId": "uuid",
      "operation": "UPSERT",
      "baseVersion": 0,
      "clientUpdatedAt": "2026-11-24T10:00:00Z",
      "payload": { "name": "Push Day" }
    }
  ]
}
```

`operation` is `UPSERT`, `DELETE`, or `RESTORE`. `DELETE` for a new record requires a payload; `RESTORE` always requires a payload.

Response results contain `mutationId`, `syncId`, `status` (`APPLIED`, `CONFLICT`, `DUPLICATE`, `REJECTED`), `serverVersion`, `serverUpdatedAt`, `deleted`, and `serverPayload` on conflict. `DUPLICATE` responses additionally carry `originalStatus`, the status stored in the original receipt (`APPLIED`, `CONFLICT`, or `REJECTED`). Clients predating this field treat a duplicate as a successful replay.

Each mutation is processed in its own transaction. A `sync_mutation_receipt` keyed by `(user_id, mutation_id)` makes retries idempotent. The request hash is computed from canonical JSON (object keys sorted recursively).

When a mutation ID is retried with the same canonical request, the stored original result is replayed with `status` changed to `DUPLICATE` and `originalStatus` set to the original status (all other fields are unchanged). A duplicate of an `APPLIED` mutation is acknowledged; a duplicate whose `originalStatus` is `CONFLICT` or `REJECTED` must keep the local mutation blocking. If `originalStatus` is absent (legacy server), the duplicate is treated as a successful replay for backward compatibility.

## GET /api/sync/changes?cursor=...&limit=100

Response:

```json
{
  "records": [
    {
      "entityType": "WORKOUT",
      "syncId": "uuid",
      "serverVersion": 1,
      "serverUpdatedAt": "2026-11-24T10:00:00Z",
      "deleted": false,
      "payload": { "name": "Push Day" }
    }
  ],
  "nextCursor": "opaque",
  "hasMore": false,
  "serverTime": "2026-11-24T10:00:01Z"
}
```

The cursor is an HMAC-signed, user-bound, expiring (24 h) base64url value containing the last consumed sequence and, while a snapshot is being paged, its high-water mark. Once the snapshot is consumed, the high-water mark is released so later writes are visible on the next sync.

If a correctly signed, user-bound, structurally valid cursor is rejected solely because its 24 h expiry has passed, `/api/sync/changes` responds `400` with `error: "CURSOR_EXPIRED"`. Clients may clear that cursor and retry once from `cursor = null`. Malformed, tampered, foreign-user and out-of-range cursors keep `error: "INVALID_REQUEST"` and must not be silently reset.

## Supported entity types
`WORKOUT`, `CUSTOM_EXERCISE`, `WORKOUT_EXERCISE`, `EXERCISE_PROGRESS`, `PLANNED_SET`, `WORKOUT_COMPLETION`, `SCHEDULED_WORKOUT`, `FAVORITE_EXERCISE`.

## Limits
Batch ≤ 100 mutations, payload ≤ 256 KB, page size 1–500 (default 100).

## Configuration
`SYNC_CURSOR_SECRET` must be set to a unique random value of at least 32 characters in production. Startup fails if it is missing, too short, or still equal to the development default. The secret is used only for HMAC signing; it is never logged or returned by the API.
