package com.spyrosoft_backend.service;

import com.spyrosoft_backend.exception.ChargerAlreadyBookedException;
import com.spyrosoft_backend.exception.ReservationInThePastException;
import com.spyrosoft_backend.exception.ReservationNotFoundException;
import com.spyrosoft_backend.model.Charger;
import com.spyrosoft_backend.model.CreateReservationRequest;
import com.spyrosoft_backend.model.Reservation;
import com.spyrosoft_backend.repository.ChargerRepository;
import com.spyrosoft_backend.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@Mock
	private ChargerRepository chargerRepository;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private Charger charger;

	private ReservationService service;

	@BeforeEach
	void setUp() {
		service = new ReservationService(chargerRepository, reservationRepository);
	}

	@Test
	void touchingBookingsOnTheSameChargerAreBothAllowed() {
		Instant newStart = Instant.parse("2026-12-01T12:00:00Z");
		when(charger.getId()).thenReturn(7L);
		when(chargerRepository.findById(7L)).thenReturn(Optional.of(charger));
		when(reservationRepository.existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(7L, newStart.plus(2, ChronoUnit.HOURS), newStart))
				.thenReturn(false);

		service.create(new CreateReservationRequest(7L, "  Anna  ", newStart, 2));

		ArgumentCaptor<Reservation> saved = ArgumentCaptor.forClass(Reservation.class);
		verify(reservationRepository).save(saved.capture());
		assertEquals("Anna", saved.getValue().getUserName());
		assertEquals(newStart.plus(2, ChronoUnit.HOURS), saved.getValue().getEndsAt());
	}

	@Test
	void overlappingBookingOnTheSameChargerIsRejected() {
		Instant newStart = Instant.parse("2026-12-01T11:00:00Z");
		when(charger.getId()).thenReturn(7L);
		when(chargerRepository.findById(7L)).thenReturn(Optional.of(charger));
		when(reservationRepository.existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(7L, newStart.plus(2, ChronoUnit.HOURS), newStart))
				.thenReturn(true);

		assertThrows(ChargerAlreadyBookedException.class,
				() -> service.create(new CreateReservationRequest(7L, "Anna", newStart, 2)));
		verify(reservationRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void bookingThatStartsInThePastIsRejectedBeforeLookingAtOtherReservations() {
		assertThrows(ReservationInThePastException.class, () -> service.create(new CreateReservationRequest(
				7L,
				"Anna",
				Instant.now().minusSeconds(5),
				2
		)));
		verify(reservationRepository, never()).existsByCharger_IdAndStartsAtBeforeAndEndsAtAfter(
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any()
		);
	}

	@Test
	void cancellingAMissingReservationDoesNotDeleteAnything() {
		when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ReservationNotFoundException.class, () -> service.deleteReservation(99L));
		verify(reservationRepository, never()).delete(org.mockito.ArgumentMatchers.any());
	}

}
