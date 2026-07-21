# Travel Itinerary Planner — Capstone Project

A full-stack desktop application for planning travel itineraries with real flight data, hotel search, budget management, and PDF export.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend UI | JavaFX 21 + FXML + CSS |
| Backend API | Spring Boot 3.2.3 |
| Flight Data | RapidAPI — Skyscanner Flights Travel API |
| Hotel Data | RapidAPI — TripAdvisor16 |
| HTTP (backend → API) | OkHttp 4.12.0 |
| HTTP (frontend → backend) | java.net.http (JDK built-in) |
| JSON Parsing | Jackson (both modules) |
| PDF Generation | iText 7 (kernel + layout + io) |
| Build | Maven (monorepo — parent POM with `backend` and `frontend` modules) |

---

## Project Structure

```
CSC325-Capstone/
├── pom.xml                  # Parent POM
├── docs/                    # API and screen documentation
│   ├── api-flights.md
│   ├── api-hotels-and-pdf.md
│   └── screen-budget-summary.md
├── backend/                 # Spring Boot REST API
│   └── src/main/
│       ├── java/com/travel/backend/
│       │   ├── TravelBackendApplication.java   # Spring Boot entry point
│       │   ├── config/
│       │   │   └── AppConfig.java      # CORS configuration
│       │   ├── controller/
│       │   │   ├── FlightController.java   # /api/flights/airports, /api/flights/search
│       │   │   ├── HotelController.java    # /api/hotels/locations, /api/hotels/search
│       │   │   └── PdfController.java      # /api/pdf/generate
│       │   ├── service/
│       │   │   ├── FlightService.java      # OkHttp calls to Skyscanner RapidAPI
│       │   │   ├── HotelService.java       # OkHttp calls to TripAdvisor16 RapidAPI
│       │   │   └── PdfService.java         # iText PDF generation
│       │   └── model/
│       │       ├── AirportResult.java
│       │       ├── FlightResult.java
│       │       ├── FlightSummaryItem.java
│       │       ├── HotelLocationResult.java
│       │       ├── HotelResult.java
│       │       ├── HotelSummaryItem.java
│       │       ├── ExpenseSummaryItem.java
│       │       └── TripSummaryRequest.java
│       └── resources/
│           └── application.properties  # Server port, RapidAPI keys/hosts
└── frontend/                # JavaFX desktop app
    └── src/main/
        ├── java/com/travel/frontend/
        │   ├── TravelApp.java              # JavaFX entry point
        │   ├── controller/
        │   │   ├── MainController.java     # Sidebar nav, loads screens into StackPane
        │   │   ├── FlightSearchController.java # Screen 1 — airport + flight search
        │   │   ├── HotelController.java    # Screen 2 — location + hotel search
        │   │   ├── BudgetController.java   # Screen 3 — manage flights/hotels/expenses, budget breakdown
        │   │   └── SummaryController.java  # Screen 4 — read-only recap + PDF export
        │   ├── model/
        │   │   ├── TripSession.java        # Singleton — shared state across all screens
        │   │   ├── FlightInfo.java         # JavaFX property model for flight TableViews
        │   │   ├── HotelInfo.java          # JavaFX property model for hotel TableViews
        │   │   └── ExpenseItem.java        # JavaFX property model for custom budget expenses
        │   └── util/
        │       └── ApiClient.java          # HTTP client wrapper (java.net.http), base: localhost:8080
        └── resources/com/travel/frontend/
            ├── css/styles.css               # App-wide styling
            └── fxml/                        # Budget.fxml, FlightSearch.fxml, Hotel.fxml, Main.fxml, Summary.fxml
```

---

## Architecture

The backend acts as a proxy between the JavaFX frontend and RapidAPI, keeping API calls and key management server-side.

```
JavaFX Frontend
     │
     │  HTTP (java.net.http)
     ▼
Spring Boot Backend (localhost:8080)
     │
     │  OkHttp
     ▼
RapidAPI — skyscanner-flights-travel-api.p.rapidapi.com (airports + flights)
RapidAPI — tripadvisor16.p.rapidapi.com (hotel locations + hotel search)
```

State is managed via `TripSession` (singleton), which accumulates selections across all screens. No database — data lives in memory for the current session.

---

## Screens

| # | Screen | What it does |
|---|---|---|
| 1 | Flight Search | Search airports, pick origin + destination, select date + cabin class, browse and add any number of flights (no round-trip API, so outbound + return are added as two separate one-way flights) |
| 2 | Hotel | Search a location to get a geoId, pick check-in/check-out dates, browse and add any number of hotels |
| 3 | Budget | Manage the trip: itemized flight/hotel breakdown with remove buttons, fixed Food/Transport/Activities fields, a flexible "Other Expenses" list (add/remove custom line items), running total |
| 4 | Summary + PDF | Read-only recap of flights, hotels, and all budget categories; export to PDF via iText |

There is no separate "Trip Info" screen — it was removed once its only remaining content (the flights/hotels lists) became redundant with Budget and Summary. Budget is the one place you add/remove flights, hotels, and expenses.

---

## API Endpoints

### Backend (Spring Boot)

| Method | Path | Description |
|---|---|---|
| GET | `/api/flights/airports?query=` | Search airports by name/city |
| GET | `/api/flights/search` | Search flights (originSkyId, destinationSkyId, originEntityId, destinationEntityId, date, adults, cabinClass) |
| GET | `/api/hotels/locations?query=` | Search hotel locations, returns a `geoId` per result |
| GET | `/api/hotels/search` | Search hotels (geoId, checkIn, checkOut) |
| POST | `/api/pdf/generate` | Generate PDF from trip summary request body (flights, hotels, budget categories, custom expenses) |

### RapidAPI (called by backend)

| Endpoint | Host | Used for |
|---|---|---|
| `/flights/searchAirport` | skyscanner-flights-travel-api.p.rapidapi.com | Airport typeahead search |
| `/flights/searchFlights` | skyscanner-flights-travel-api.p.rapidapi.com | Flight results |
| `/api/v1/hotels/searchLocation` | tripadvisor16.p.rapidapi.com | Hotel location typeahead search (returns `geoId`) |
| `/api/v1/hotels/searchHotels` | tripadvisor16.p.rapidapi.com | Hotel results for a geoId + date range |

---

## Key Decisions & Notes

- **No Lombok** — removed due to incompatibility with Java 24; all models use plain getters/setters.
- **No RestTemplate** — replaced with OkHttp in the backend per project requirements.
- **Allowed HTTP clients**: AsyncHttpClient, java.net.http, OkHttp, Unirest. OkHttp used in backend, java.net.http in frontend.
- **URL encoding** — query parameters are added via OkHttp's `HttpUrl.Builder.addQueryParameter()`, which encodes spaces and special characters automatically; no manual `URLEncoder` calls needed.
- **TripSession** — singleton, no database. All trip data (flights, hotels, budget) lives here for the session lifetime.
- **Flight number not shown** — the Skyscanner API response does not include individual flight numbers in the itinerary structure; the column was removed from the UI.
- **AirportResult.subtitle** — stores `countryName` from the API response, displayed in the airport list as `"Name — Country"`.
- **Flights and hotels are lists, not single selections** — there's no round-trip flight search, so a trip needs at least two one-way flights (outbound + return); hotels can also be multiple for multi-city trips. Both lists are unlimited and managed from the Budget tab.
- **Hotel price is per-night, not total-for-stay** — verified by comparing the same hotel's `priceForDisplay` across different-length date ranges (price stayed flat rather than scaling with nights).
- **Longer HTTP timeouts on both RapidAPI clients** — `HotelService` and `FlightService` both use OkHttpClients with 30s connect/read/write timeouts (vs. OkHttp's 10s default), since both the Skyscanner and TripAdvisor16 endpoints have been observed to occasionally take longer than 10s to respond.
- **FXML `$` escaping** — any literal `$` in an FXML attribute value (e.g. `text="$0.00"`) must be escaped as `\$0.00`, otherwise JavaFX's FXMLLoader tries to parse it as an expression binding and throws `IllegalArgumentException: Invalid path`. (The older `$$` escape also works but is deprecated in this JavaFX version.)
- **No "cost per traveler"** — removed from both Budget and Summary for now; only the estimated total is shown.

---

## Running the App

**Start the backend:**
```bash
cd backend
mvn spring-boot:run
```

**Start the frontend** (in a separate terminal):
```bash
cd frontend
mvn javafx:run
```

Backend must be running before launching the frontend. After any backend change, restart it (no hot-reload). After any frontend FXML/resource change, run `mvn clean package` before `mvn javafx:run` to avoid stale compiled files in `target/`.
