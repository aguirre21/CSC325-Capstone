package com.travel.backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.travel.backend.model.ExpenseSummaryItem;
import com.travel.backend.model.FlightSummaryItem;
import com.travel.backend.model.HotelSummaryItem;
import com.travel.backend.model.TripSummaryRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    private static final DeviceRgb PRIMARY = new DeviceRgb(13, 71, 161);
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(238, 242, 247);
    private static final DeviceRgb TEXT_SECONDARY = new DeviceRgb(84, 110, 122);

    public byte[] generateTripSummary(TripSummaryRequest req) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.setMargins(40, 50, 40, 50);

            addHeader(doc, req);
            addSectionSpacer(doc);
            addFlightDetails(doc, req);
            addSectionSpacer(doc);
            addHotelDetails(doc, req);
            addSectionSpacer(doc);
            addBudgetBreakdown(doc, req);
            addFooter(doc);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private void addHeader(Document doc, TripSummaryRequest req) {
        Paragraph title = new Paragraph("Travel Itinerary")
                .setFontSize(28)
                .setBold()
                .setFontColor(PRIMARY)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(title);

        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1.5f))
                .setMarginTop(10).setMarginBottom(10));
    }

    private void addFlightDetails(Document doc, TripSummaryRequest req) {
        List<FlightSummaryItem> flights = req.getFlights();
        if (flights == null || flights.isEmpty()) return;
        addSectionTitle(doc, "Flight Information");
        for (int i = 0; i < flights.size(); i++) {
            FlightSummaryItem f = flights.get(i);
            String label = flights.size() == 2 ? (i == 0 ? "Outbound Flight" : "Return Flight") : "Flight " + (i + 1);
            Table table = twoColTable();
            addRow(table, label, "");
            addRow(table, "Airline", f.getAirline());
            addRow(table, "Flight Number", f.getFlightNumber());
            addRow(table, "Route", f.getOrigin() + " → " + f.getDestination());
            addRow(table, "Departure", f.getDeparture());
            addRow(table, "Arrival", f.getArrival());
            addRow(table, "Duration", formatDuration(f.getDurationMinutes()));
            addRow(table, "Stops", f.getStops() == 0 ? "Nonstop" : f.getStops() + " stop(s)");
            addRow(table, "Flight Cost", String.format("$%.2f", f.getCost()));
            doc.add(table);
            if (i < flights.size() - 1) addSectionSpacer(doc);
        }
    }

    private double totalFlightCost(TripSummaryRequest req) {
        if (req.getFlights() == null) return 0.0;
        return req.getFlights().stream().mapToDouble(FlightSummaryItem::getCost).sum();
    }

    private void addHotelDetails(Document doc, TripSummaryRequest req) {
        List<HotelSummaryItem> hotels = req.getHotels();
        if (hotels == null || hotels.isEmpty()) return;
        addSectionTitle(doc, "Hotel Information");
        for (int i = 0; i < hotels.size(); i++) {
            HotelSummaryItem h = hotels.get(i);
            Table table = twoColTable();
            addRow(table, "Hotel", h.getName());
            addRow(table, "Location", h.getLocation());
            addRow(table, "Check-in", h.getCheckIn());
            addRow(table, "Check-out", h.getCheckOut());
            addRow(table, "Nights", String.valueOf(h.getNights()));
            addRow(table, "Cost per Night", String.format("$%.2f", h.getPricePerNight()));
            addRow(table, "Total Hotel Cost", String.format("$%.2f", h.getTotalCost()));
            doc.add(table);
            if (i < hotels.size() - 1) addSectionSpacer(doc);
        }
    }

    private double totalHotelCost(TripSummaryRequest req) {
        if (req.getHotels() == null) return 0.0;
        return req.getHotels().stream().mapToDouble(HotelSummaryItem::getTotalCost).sum();
    }

    private double totalOtherExpenses(TripSummaryRequest req) {
        if (req.getOtherExpenses() == null) return 0.0;
        return req.getOtherExpenses().stream().mapToDouble(ExpenseSummaryItem::getAmount).sum();
    }

    private void addBudgetBreakdown(Document doc, TripSummaryRequest req) {
        addSectionTitle(doc, "Budget Breakdown");

        double flightTotal = totalFlightCost(req);
        double hotelTotal = totalHotelCost(req);
        double otherTotal = totalOtherExpenses(req);
        double total = flightTotal + hotelTotal + req.getFoodBudget()
                + req.getTransportBudget() + req.getActivitiesBudget() + otherTotal;

        Table table = twoColTable();
        addRow(table, "Flights", String.format("$%.2f", flightTotal));
        addRow(table, "Hotel", String.format("$%.2f", hotelTotal));
        addRow(table, "Food & Dining", String.format("$%.2f", req.getFoodBudget()));
        addRow(table, "Transportation", String.format("$%.2f", req.getTransportBudget()));
        addRow(table, "Activities", String.format("$%.2f", req.getActivitiesBudget()));
        addRow(table, "Other", String.format("$%.2f", otherTotal));
        doc.add(table);

        if (req.getOtherExpenses() != null && !req.getOtherExpenses().isEmpty()) {
            Table otherTable = twoColTable();
            for (ExpenseSummaryItem e : req.getOtherExpenses()) {
                addRow(otherTable, e.getDescription(), String.format("$%.2f", e.getAmount()));
            }
            doc.add(otherTable.setMarginTop(4));
        }

        Table totals = twoColTable();
        Cell totalLabel = new Cell().add(new Paragraph("ESTIMATED TOTAL").setBold().setFontColor(PRIMARY));
        Cell totalValue = new Cell().add(new Paragraph(String.format("$%.2f", total)).setBold().setFontColor(PRIMARY));
        totals.addCell(totalLabel);
        totals.addCell(totalValue);
        doc.add(totals.setMarginTop(8));
    }

    private void addFooter(Document doc) {
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f)).setMarginTop(20));
        doc.add(new Paragraph("Generated by Travel Itinerary Planner")
                .setFontSize(9)
                .setFontColor(TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6));
    }

    private void addSectionTitle(Document doc, String title) {
        doc.add(new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY)
                .setMarginBottom(6));
    }

    private void addSectionSpacer(Document doc) {
        doc.add(new Paragraph(" ").setMarginBottom(4));
    }

    private Table twoColTable() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontColor(TEXT_SECONDARY))
                .setBackgroundColor(LIGHT_BG).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-"))
                .setPadding(6));
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + m + "m";
    }
}
