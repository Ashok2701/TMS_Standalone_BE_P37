# POD API — Integration Guide

Base URL: `https://tmssolutions.tema-systems.com:8040`

All endpoints below are under `/api/pod`. Every response — success or
failure — is JSON. On failure, every endpoint returns:

```json
HTTP 400 Bad Request
{ "message": "<human-readable reason>" }
```

except an outright missing/invalid/expired token, which returns:

```json
HTTP 401 Unauthorized
{ "message": "Authentication required — please log in again" }
```

---

## Authentication

### `POST /api/pod/auth/login`

No token required.

**Request body**
```json
{
  "username": "driver_username",
  "password": "driver_password"
}
```

**Response — 200 OK**
```json
{
  "accessToken": "eyJhbGciOi...",
  "driverId": "DN072",
  "driverName": "Ranjeet Transport Driver 3",
  "username": "ranjeet.driver3",
  "site": "11001",
  "mobileNo": "+18681234567"
}
```

**Failure reasons** (all HTTP 400):
- `"Username and password are required"`
- `"Driver not found"`
- `"No password set for this driver — contact an administrator"`
- `"Password is wrong"`
- `"Driver is inactive"`

### Using the token

Every other `/api/pod/**` endpoint requires this header on every request:

```
Authorization: Bearer <accessToken>
```

The token identifies which driver is calling — **every endpoint below
is automatically scoped to that driver**. You never pass a `driverId`
anywhere; the server determines it from the token and rejects any
document/trip that doesn't actually belong to that driver, even if the
docNum/tripCode is otherwise valid.

Tokens do not currently expire in a meaningful timeframe for this
integration (long-lived) — if you get a 401, re-login.

---

## `GET /api/pod/trips`

The authenticated driver's trips for a given date.

**Query params**
| param | required | format | notes |
|---|---|---|---|
| `date` | no | `YYYY-MM-DD` | defaults to today if omitted |

**Response — 200 OK**
```json
[
  {
    "tripCode": "VR-11001-20260807-001",
    "site": "11001",
    "vehicleCode": "14BAY10",
    "driverName": "Ranjeet Transport Driver 3",
    "status": "Locked",
    "depSite": "11001",
    "arrSite": "11001",
    "startTime": "07:00",
    "stops": 5,
    "drops": 4,
    "pickups": 1
  }
]
```
`status` is one of: `Open`, `Optimised`, `Locked`, `Validated`. Only a
`Locked` or `Validated` trip is realistically ready for a driver to
start working — earlier statuses mean the trip is still being planned.

Empty array if the driver has no trips that day (not an error).

---

## `GET /api/pod/trips/{tripCode}/stops`

Every stop on a trip, in planned sequence order. Fails if the trip
isn't assigned to the authenticated driver.

**Response — 200 OK**
```json
[
  {
    "docNum": "PIC110010341",
    "seq": 1,
    "type": "DROP",
    "docType": "PICK",
    "clientName": "BLACK'S CAFE & MINI MART",
    "bpCode": "110001",
    "address": "5A Prince St",
    "city": "ARIM",
    "postalCity": ", ARIM",
    "qty": 5,
    "weight": 47.315,
    "weightUnit": "KG",
    "status": "PENDING"
  }
]
```
`status` is `"PENDING"` until a POD has been submitted for that
document, after which it reflects the submitted POD's status
(`DELIVERED` / `PARTIAL` / `FAILED` / `REFUSED`).

**Failure reasons**: `"Trip not found: <code>"`, `"This trip is not
assigned to you"`.

---

## `GET /api/pod/stops/{docNum}`

Full detail for one stop, including its product lines — use this
before showing the delivery/pickup confirmation screen.

**Response — 200 OK**
```json
{
  "docNum": "PIC110010341",
  "seq": 1,
  "type": "DROP",
  "docType": "PICK",
  "clientName": "BLACK'S CAFE & MINI MART",
  "bpCode": "110001",
  "address": "5A Prince St",
  "city": "ARIM",
  "postalCity": ", ARIM",
  "arrivalTime": "07:43",
  "departureTime": "08:13",
  "qty": 5,
  "weight": 47.315,
  "weightUnit": "KG",
  "status": "PENDING",
  "products": [
    {
      "itemCode": "410001",
      "description": "2.5G BIB COKE NO SUGAR 2.5G",
      "qtyOrdered": 5,
      "netWeight": 10,
      "weightUnit": "KG",
      "volume": 10,
      "volumeUnit": "M3"
    }
  ]
}
```

**Failure reason**: `"Document <docNum> was not found on any of your
trips"` — covers both a genuinely non-existent docNum and one that
belongs to a different driver; the response is identical either way,
deliberately, so this endpoint doesn't leak whether a document exists
under someone else's trips.

---

## `POST /api/pod/stops/{docNum}/complete`

Submit the proof of delivery/pickup for a stop. Can be called again
for the same `docNum` to update/correct a previous submission (it
overwrites, not appends).

**Request body**
```json
{
  "status": "DELIVERED",
  "recipientName": "John Doe",
  "recipientRelation": "Store Manager",
  "signatureBase64": "data:image/png;base64,iVBORw0KGgo...",
  "photosBase64": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
  ],
  "remarks": "Left at front desk",
  "failureReason": null,
  "latitude": 10.6549,
  "longitude": -61.4995
}
```

| field | required | notes |
|---|---|---|
| `status` | **yes** | one of `DELIVERED`, `PARTIAL`, `FAILED`, `REFUSED` |
| `recipientName` | no | |
| `recipientRelation` | no | e.g. "Store Manager", "Security" |
| `signatureBase64` | no | full data URL, not bare base64 |
| `photosBase64` | no | array of data URLs |
| `remarks` | no | free text |
| `failureReason` | **required if status is FAILED or REFUSED** | ignored/cleared for DELIVERED/PARTIAL |
| `latitude` / `longitude` | no | GPS at time of completion |

**Response — 200 OK** — same shape as `GET .../pod` below.

**Failure reasons**:
- `"Document <docNum> was not found on any of your trips"`
- `"status must be one of [DELIVERED, PARTIAL, FAILED, REFUSED]"`
- `"failureReason is required when status is FAILED"` (or `REFUSED`)

---

## `GET /api/pod/stops/{docNum}/pod`

Retrieve a previously submitted POD (e.g. for a review/confirmation screen).

**Response — 200 OK**
```json
{
  "podId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "docNum": "PIC110010341",
  "tripCode": "VR-11001-20260807-001",
  "driverId": "DN072",
  "status": "DELIVERED",
  "recipientName": "John Doe",
  "recipientRelation": "Store Manager",
  "signatureBase64": "data:image/png;base64,...",
  "photosBase64": ["data:image/jpeg;base64,..."],
  "remarks": "Left at front desk",
  "failureReason": null,
  "latitude": 10.6549,
  "longitude": -61.4995,
  "deliveredAt": "2026-08-07T14:32:00"
}
```

**Failure reasons**: same ownership check as the other endpoints, plus
`"No POD submitted yet for <docNum>"` if the stop exists but nothing's
been submitted for it.

---

## Required database migration

This project does not auto-manage schema (`ddl-auto=none`, no
Flyway/Liquibase). Run this against the database **before** deploying
this backend version, or every driver-related and POD request will
fail with a missing-column/table error:

```sql
ALTER TABLE tms.xr_driver
  ADD COLUMN username VARCHAR(100) UNIQUE,
  ADD COLUMN password VARCHAR(255);

CREATE TABLE tms.xr_pod (
    pod_id              UUID PRIMARY KEY,
    doc_num             VARCHAR(50) NOT NULL UNIQUE,
    trip_code           VARCHAR(60) NOT NULL,
    driver_id           VARCHAR(30) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    recipient_name      VARCHAR(150),
    recipient_relation  VARCHAR(100),
    signature           TEXT,
    photos              TEXT,
    remarks             VARCHAR(500),
    failure_reason      VARCHAR(200),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    delivered_at        TIMESTAMP NOT NULL,
    created_at          TIMESTAMP NOT NULL
);
```

Each driver also needs a `username`/`password` set (via the existing
`PUT /api/drivers/{driverId}` endpoint, or directly in the database)
before they can log in — there is currently no self-service
"set my password" flow.

---

## Known limitations (current version)

- **No file storage service** — signatures and photos travel as
  base64 data URLs in the JSON body and are stored as-is in the
  database (`TEXT` columns). No size limit is enforced server-side yet;
  keep photos reasonably compressed on the device before sending.
- **No X3 sync yet** — completing a POD updates only this system's own
  `xr_pod` table. Whether/when that should also push delivery status
  back to X3 (`SDELIVERY`/`STOPREH`) is a separate, not-yet-built piece.
- **No offline support** — the app must have connectivity to call
  these endpoints; there's no queued/retry mechanism built in on the
  server side (any offline handling would need to live in the mobile
  app itself, retrying the same `complete` call once back online —
  it's safe to retry since submitting again for the same `docNum`
  overwrites rather than duplicates).
- **No password self-service** — a driver can't currently reset their
  own password from the app; that has to go through an administrator
  updating the Driver record.
