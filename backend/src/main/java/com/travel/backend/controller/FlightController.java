package com.travel.backend.controller;

import com.travel.backend.model.AirportResult;
import com.travel.backend.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/airports")
    public ResponseEntity<List<AirportResult>> searchAirports(@RequestParam String query) {
        return ResponseEntity.ok(flightService.searchAirports(query));
    }
}
