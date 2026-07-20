# Hotels and PDF API

## GET /api/hotels/locations

Searches hotel locations by text query.

### Request

Query parameters:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `query` | string | yes | Location search text. |

### Response

Returns JSON:

```json
[
  {
    "geoId": 60763,
    "name": "New York City",
    "subtitle": "New York"
  }
]
```

### cURL

```bash
curl "http://localhost:8080/api/hotels/locations?query=New%20York"
```

## GET /api/hotels/search

Searches hotels for a selected TripAdvisor `geoId` and date range.

### Request

Query parameters:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `geoId` | integer | yes | TripAdvisor hotel location ID. |
| `checkIn` | string | yes | Check-in date in `YYYY-MM-DD` format. |
| `checkOut` | string | yes | Check-out date in `YYYY-MM-DD` format. |

### Response

Returns JSON:

```json
[
  {
    "id": "123456",
    "name": "Example Hotel",
    "location": "Midtown",
    "rating": 4.5,
    "reviewCount": "1,234",
    "pricePerNight": 199.0,
    "priceFormatted": "$199"
  }
]
```

### cURL

```bash
curl "http://localhost:8080/api/hotels/search?geoId=60763&checkIn=2026-08-01&checkOut=2026-08-05"
```

## POST /api/pdf/generate

Generates a trip summary PDF.

### Request

Content type: `application/json`

```json
{
  "tripName": "Summer Trip",
  "destination": "New York City",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "flights": [
    {
      "airline": "Delta",
      "flightNumber": "DL123",
      "departureAirport": "ATL",
      "arrivalAirport": "JFK",
      "departureTime": "2026-08-01T09:00:00",
      "arrivalTime": "2026-08-01T11:15:00",
      "price": 250.0,
      "priceFormatted": "$250.00"
    }
  ],
  "hotels": [
    {
      "name": "Example Hotel",
      "location": "Midtown",
      "checkIn": "2026-08-01",
      "checkOut": "2026-08-05",
      "rating": 4.5,
      "reviewCount": "1,234",
      "pricePerNight": 199.0,
      "priceFormatted": "$199"
    }
  ],
  "expenses": [
    {
      "category": "Food",
      "description": "Meals",
      "amount": 200.0
    }
  ]
}
```

`flights`, `hotels`, and `expenses` may be empty arrays.

### Response

Returns a PDF file.

Headers:

```text
Content-Type: application/pdf
Content-Disposition: attachment; filename="trip-summary.pdf"
```

### cURL

```bash
curl -X POST "http://localhost:8080/api/pdf/generate" \
  -H "Content-Type: application/json" \
  -o trip-summary.pdf \
  -d '{
    "tripName": "Summer Trip",
    "destination": "New York City",
    "startDate": "2026-08-01",
    "endDate": "2026-08-05",
    "flights": [],
    "hotels": [],
    "expenses": [
      {
        "category": "Food",
        "description": "Meals",
        "amount": 200.0
      }
    ]
  }'
```
