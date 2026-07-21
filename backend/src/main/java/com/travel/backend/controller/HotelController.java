package com.travel.backend.controller;

import com.travel.backend.model.HotelLocationResult;
import com.travel.backend.model.HotelResult;
import com.travel.backend.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<HotelLocationResult>> searchLocations(@RequestParam String query) {
        return ResponseEntity.ok(hotelService.searchLocations(query));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResult>> searchHotels(
            @RequestParam int geoId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        return ResponseEntity.ok(hotelService.searchHotels(geoId, checkIn, checkOut));
    }
}
