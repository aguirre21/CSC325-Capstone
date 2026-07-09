package com.travel.backend.controller;

import com.travel.backend.model.HotelLocationResult;
import com.travel.backend.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
