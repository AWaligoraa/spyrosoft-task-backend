package com.spyrosoft_backend.repository;

import com.spyrosoft_backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(Long chargerId, Instant endsAt, Instant startsAt);

	List<Reservation> findByCharger_IdIn(Collection<Long> chargerIds);
}
