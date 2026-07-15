# Flight API Endpoints

Owner: Rigens (backend flight search). Both endpoints live in `FlightController.java` and are backed by `FlightService.java`.

---

## 1. Airport Search

```
GET /api/flights/airports?query=<string>
```

Looks up airports/cities matching a free-text query. Use this first to get the `skyId` and `entityId` values that flight search requires.

### Request params

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| query | string | yes | Free text, e.g. `New York` or `LHR` |

### Example

```
curl "http://localhost:8080/api/flights/airports?query=New%20York"
```

```json
[
  {
    "skyId": "NYCA",
    "entityId": "27537542",
    "name": "New York",
    "subtitle": "United States"
  },
  {
    "skyId": "JFK",
    "entityId": "95565058",
    "name": "New York John F. Kennedy",
    "subtitle": "United States"
  }
]
```

### Field meanings

| Field | Meaning |
|-------|---------|
| skyId | Airport/city code used as `originSkyId` / `destinationSkyId` in flight search |
| entityId | Internal ID used as `originEntityId` / `destinationEntityId` in flight search |
| name | Display name of the airport or city |
| subtitle | Country name (for display next to `name`) |

No matches returns an empty array `[]`, not an error.

---

## 2. Flight Search

```
GET /api/flights/search?originSkyId=...&destinationSkyId=...&originEntityId=...&destinationEntityId=...&date=YYYY-MM-DD&adults=<int>&cabinClass=<class>
```

Searches one-way flights between two airports on a date and returns parsed itineraries.

### Request params

| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| originSkyId | string | yes | — | From airport search `skyId` |
| destinationSkyId | string | yes | — | From airport search `skyId` |
| originEntityId | string | yes | — | From airport search `entityId` |
| destinationEntityId | string | yes | — | From airport search `entityId` |
| date | string | yes | — | Departure date, `YYYY-MM-DD` |
| adults | int | no | 1 | Number of adult passengers |
| cabinClass | string | no | economy | One of `economy`, `premium_economy`, `business`, `first` |

### Example

```
curl "http://localhost:8080/api/flights/search?originSkyId=JFK&destinationSkyId=LHR&originEntityId=95565058&destinationEntityId=95565050&date=2026-08-01&adults=1&cabinClass=economy"
```

```json
[
  {
    "itineraryId": "13554-2608011700--32171-1-12712-2608021445",
    "airline": "JetBlue",
    "flightNumber": null,
    "originCode": "JFK",
    "destinationCode": "LHR",
    "originCity": null,
    "destinationCity": null,
    "departureTime": "2026-08-01T17:00:00",
    "arrivalTime": "2026-08-02T14:45:00",
    "durationMinutes": 605,
    "stopCount": 1,
    "priceRaw": 329.0,
    "priceFormatted": "USD 329.00"
  }
]
```

### Field meanings

| Field | Meaning |
|-------|---------|
| itineraryId | Unique ID for the itinerary from the flight API |
| airline | Name of the first carrier on the first leg |
| originCode / destinationCode | Airport codes for the first leg |
| departureTime / arrivalTime | Local ISO timestamps |
| durationMinutes | Total leg duration in minutes |
| stopCount | Number of stops (0 = direct) |
| priceRaw | Numeric price in USD |
| priceFormatted | Display-ready price string |

### Always-null fields (important for frontend)

`flightNumber`, `originCity`, and `destinationCity` exist in the JSON shape but **always come back null/blank** — the underlying RapidAPI response does not supply them. The frontend should render these as "N/A"/"" and must not build features that depend on them.

### Error behavior

- No itineraries available (bad route, past date, no matches) → empty array `[]`, not a 500
- A single malformed itinerary in the API response is skipped; the rest still return
