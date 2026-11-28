package com.spyrosoft_backend.controller;

import com.spyrosoft_backend.model.CreateReservationRequest;
import com.spyrosoft_backend.model.Reservation;
import com.spyrosoft_backend.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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

	@Operation(summary = "Book a charger")
	@PostMapping
	public Reservation create(@Valid @RequestBody CreateReservationRequest request) {
		return reservationService.create(request);
	}

	@Operation(summary = "List reservations")
	@GetMapping
	public List<Reservation> reservations() {
		return reservationService.getReservations();
	}

	@Operation(summary = "Cancel a reservation")
	@DeleteMapping("/{id}")
	public Reservation delete(@PathVariable Long id) {
		return reservationService.deleteReservation(id);
	}
}
