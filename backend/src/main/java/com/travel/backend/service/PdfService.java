package com.travel.backend.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.travel.backend.model.ExpenseSummaryItem;
import com.travel.backend.model.FlightSummaryItem;
import com.travel.backend.model.HotelSummaryItem;
import com.travel.backend.model.TripSummaryRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] generateTripSummary(TripSummaryRequest request) {
        try {
            TripSummaryRequest summaryRequest = request == null ? new TripSummaryRequest() : request;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);

            try (Document document = new Document(pdfDocument)) {
                addTitle(document, "Trip Summary");
                addTripDetails(document, summaryRequest);
                addFlightsSection(document, listOrEmpty(summaryRequest.getFlights()));
                addHotelsSection(document, listOrEmpty(summaryRequest.getHotels()));
                addExpensesSection(document, listOrEmpty(summaryRequest.getExpenses()));
                addTotalSection(document, summaryRequest);
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate trip summary PDF: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, String title) {
        document.add(new Paragraph(title)
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addSectionHeading(Document document, String heading) {
        document.add(new Paragraph(heading)
                .setBold()
                .setFontSize(14)
                .setMarginTop(16));
    }

    private void addTripDetails(Document document, TripSummaryRequest request) {
        addSectionHeading(document, "Trip Details");

        Table table = new Table(UnitValue.createPercentArray(new float[] {1, 2}))
                .useAllAvailableWidth();
        addRow(table, "Trip Name", safe(request.getTripName()));
        addRow(table, "Destination", safe(request.getDestination()));
        addRow(table, "Start Date", safe(request.getStartDate()));
        addRow(table, "End Date", safe(request.getEndDate()));
        document.add(table);
    }

    private void addFlightsSection(Document document, List<FlightSummaryItem> flights) {
        addSectionHeading(document, "Flights");

        if (flights.isEmpty()) {
            document.add(new Paragraph("No flights selected."));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] {2, 1, 2, 2, 2, 2, 1}))
                .useAllAvailableWidth();
        addHeader(table, "Airline");
        addHeader(table, "Flight");
        addHeader(table, "From");
        addHeader(table, "To");
        addHeader(table, "Depart");
        addHeader(table, "Arrive");
        addHeader(table, "Price");

        for (FlightSummaryItem flight : flights) {
            table.addCell(safe(flight.getAirline()));
            table.addCell(safe(flight.getFlightNumber()));
            table.addCell(safe(flight.getDepartureAirport()));
            table.addCell(safe(flight.getArrivalAirport()));
            table.addCell(safe(flight.getDepartureTime()));
            table.addCell(safe(flight.getArrivalTime()));
            table.addCell(formatPrice(flight.getPriceFormatted(), flight.getPrice()));
        }

        document.add(table);
    }

    private void addHotelsSection(Document document, List<HotelSummaryItem> hotels) {
        addSectionHeading(document, "Hotels");

        if (hotels.isEmpty()) {
            document.add(new Paragraph("No hotels selected."));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] {2, 2, 1, 1, 1, 1}))
                .useAllAvailableWidth();
        addHeader(table, "Name");
        addHeader(table, "Location");
        addHeader(table, "Check In");
        addHeader(table, "Check Out");
        addHeader(table, "Rating");
        addHeader(table, "Nightly Price");

        for (HotelSummaryItem hotel : hotels) {
            table.addCell(safe(hotel.getName()));
            table.addCell(safe(hotel.getLocation()));
            table.addCell(safe(hotel.getCheckIn()));
            table.addCell(safe(hotel.getCheckOut()));
            table.addCell(String.valueOf(hotel.getRating()));
            table.addCell(formatPrice(hotel.getPriceFormatted(), hotel.getPricePerNight()));
        }

        document.add(table);
    }

    private void addExpensesSection(Document document, List<ExpenseSummaryItem> expenses) {
        addSectionHeading(document, "Expenses");

        if (expenses.isEmpty()) {
            document.add(new Paragraph("No additional expenses entered."));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[] {2, 4, 1}))
                .useAllAvailableWidth();
        addHeader(table, "Category");
        addHeader(table, "Description");
        addHeader(table, "Amount");

        for (ExpenseSummaryItem expense : expenses) {
            table.addCell(safe(expense.getCategory()));
            table.addCell(safe(expense.getDescription()));
            table.addCell(formatCurrency(expense.getAmount()));
        }

        document.add(table);
    }

    private void addTotalSection(Document document, TripSummaryRequest request) {
        addSectionHeading(document, "Estimated Total");
        double total = 0;

        for (FlightSummaryItem flight : listOrEmpty(request.getFlights())) {
            total += flight.getPrice();
        }

        for (HotelSummaryItem hotel : listOrEmpty(request.getHotels())) {
            total += hotel.getPricePerNight();
        }

        for (ExpenseSummaryItem expense : listOrEmpty(request.getExpenses())) {
            total += expense.getAmount();
        }

        document.add(new Paragraph(formatCurrency(total)).setBold());
    }

    private void addHeader(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setBold()));
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(value);
    }

    private String formatPrice(String formattedPrice, double price) {
        if (formattedPrice != null && !formattedPrice.isBlank()) {
            return formattedPrice;
        }

        return formatCurrency(price);
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value;
    }

    private <T> List<T> listOrEmpty(List<T> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        return items;
    }
}
