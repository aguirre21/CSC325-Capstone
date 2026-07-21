package com.travel.backend.controller;

import com.travel.backend.model.TripSummaryRequest;
import com.travel.backend.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody TripSummaryRequest request) {
        byte[] pdf = pdfService.generateTripSummary(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "trip-summary.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
