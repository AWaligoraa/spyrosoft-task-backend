package com.spyrosoft_backend.controller;

import com.spyrosoft_backend.model.CreateReservationRequest;
import com.spyrosoft_backend.model.Reservation;
import com.spyrosoft_backend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	public Reservation create(@Valid @RequestBody CreateReservationRequest request) {
		return reservationService.create(request);
	}

	@GetMapping
	public List<Reservation> reservations() {
		return reservationService.getReservations();
	}

	@DeleteMapping("/{id}")
	public Reservation delete(@PathVariable Long id) {
		return reservationService.deleteReservation(id);
	}
}
