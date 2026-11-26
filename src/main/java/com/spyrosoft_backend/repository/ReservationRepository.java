package com.spyrosoft_backend.repository;

import com.spyrosoft_backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(Long chargerId, Instant endsAt, Instant startsAt);
}
