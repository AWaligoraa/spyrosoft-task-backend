package com.spyrosoft_backend.service;

import com.spyrosoft_backend.exception.ChargerAlreadyBookedException;
import com.spyrosoft_backend.exception.ChargerNotFoundException;
import com.spyrosoft_backend.exception.ReservationInThePastException;
import com.spyrosoft_backend.model.Charger;
import com.spyrosoft_backend.model.CreateReservationRequest;
import com.spyrosoft_backend.model.Reservation;
import com.spyrosoft_backend.repository.ChargerRepository;
import com.spyrosoft_backend.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

	private final ChargerRepository chargerRepository;
	private final ReservationRepository reservationRepository;

	public ReservationService(ChargerRepository chargerRepository, ReservationRepository reservationRepository) {
		this.chargerRepository = chargerRepository;
		this.reservationRepository = reservationRepository;
	}

	@Transactional
	public Reservation create(CreateReservationRequest request) {
		if (request.startsAt().isBefore(Instant.now())) {
			throw new ReservationInThePastException();
		}

		Charger charger = chargerRepository.findById(request.chargerId())
				.orElseThrow(ChargerNotFoundException::new);
		Instant endsAt = request.startsAt().plus(request.hours(), ChronoUnit.HOURS);

		boolean overlaps = reservationRepository.existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(
				charger.getId(),
				endsAt,
				request.startsAt()
		);
		if (overlaps) {
			throw new ChargerAlreadyBookedException();
		}

		return reservationRepository.save(new Reservation(
				charger,
				request.userName().trim(),
				request.startsAt(),
				endsAt
		));
	}
}
