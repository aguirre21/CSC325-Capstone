# Budget & Summary Screens

Covers the two frontend screens owned by this lane: `Budget.fxml` /
`BudgetController.java` and `Summary.fxml` / `SummaryController.java`. Both read from and
write to the shared `TripSession` singleton (`frontend/src/main/java/com/travel/frontend/model/TripSession.java`),
which holds everything a user has added on the Flights, Hotel, and Budget screens.

## Budget screen

Lets the user review and edit trip costs before generating a summary:

- **Flights / Hotels breakdown** — read-only line items (pulled from `TripSession`) with a
  ✕ button per row. **This is the only screen where flights or hotels can be removed** —
  removing a row here calls `TripSession.removeFlight` / `removeHotel` directly.
- **Additional Expenses** — three fixed fields (Food & Dining, Local Transportation,
  Activities & Tours). Values are only committed to `TripSession` when "Save Budget" is
  clicked.
- **Other Expenses** — a free-form list of description/amount pairs (`ExpenseItem`), addable
  and removable from this screen only.
- **Cost Summary** — live total across flights + hotels + the three fixed categories + other
  expenses, recalculated on every field edit.

## Summary screen

A read-only recap of the same `TripSession` state, plus PDF export. Unlike Budget, **nothing
can be added or removed here** — no ✕ buttons, no editable fields. If a flight, hotel, or
expense is wrong, go back to the screen that owns it (Flights, Hotel, or Budget).

Sections: Flights, Hotels, Other Expenses (including the three fixed budget categories),
and an Estimated Total that matches the Budget screen's total exactly, since both read from
the same `TripSession`.

"Generate PDF Summary" opens a file save dialog, then POSTs to Ayush's
`/api/pdf/generate` endpoint on a background thread and writes the returned bytes to disk.

## PDF export request contract

`SummaryController.generatePdf()` builds the request body by hand as a `Map<String, Object>`
(serialized by `ApiClient.postForBytes`) — it does not depend on the backend's model classes,
since frontend and backend are separate Maven modules. The field names must match the
backend's `TripSummaryRequest` (see `docs/api-hotels-and-pdf.md`), which is **not** the same
shape as `TripSession`:

| Request field | Source |
| --- | --- |
| `tripName` | Derived from `TripSession.origin` + `destination` (e.g. `"JFK → CDG"`); falls back to `"<destination> Trip"` or `"Trip Summary"` if unset. |
| `destination` | `TripSession.destination` |
| `startDate` / `endDate` | `TripSession.departureDate` / `returnDate` |
| `flights[]` | One entry per `FlightInfo`: `airline`, `flightNumber`, `departureAirport`/`arrivalAirport` (IATA codes), `departureTime`/`arrivalTime` (raw ISO strings), `price`, `priceFormatted`. |
| `hotels[]` | One entry per `HotelInfo`: `name`, `location`, `checkIn`, `checkOut`, `pricePerNight`, `priceFormatted`. `HotelInfo` doesn't retain a raw numeric rating or review count, so those fields are omitted from the request. |
| `expenses[]` | One entry per non-zero fixed budget category (`category` = `description` = the category name) plus one entry per `ExpenseItem` in "Other Expenses" (`category` = `"Other"`). Zero-amount categories are skipped. |

Note the backend's PDF total only sums `flight.price` + `hotel.pricePerNight` (not
`pricePerNight × nights`) + `expenses[].amount` — see `PdfService.addTotalSection`. The
in-app Cost Summary total (which does multiply hotel cost by nights) may not exactly match
the total printed on the PDF for multi-night stays; this is a backend PDF total quirk, not a
frontend bug.

## Testing

```
cd backend && mvn spring-boot:run
```
```
cd frontend && mvn javafx:run
```

Add a flight and a hotel, set a budget on the Budget screen, then open Summary and confirm
the recap matches the Budget tab. Click "Generate PDF Summary," pick a save location, and
confirm a real PDF is written and opens showing the flight/hotel/budget data.
