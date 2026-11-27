package com.spyrosoft_backend.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateReservationRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void oneAndSixHoursAreAccepted() {
		assertTrue(validator.validate(request(1)).isEmpty());
		assertTrue(validator.validate(request(6)).isEmpty());
	}

	@Test
	void zeroAndSevenHoursAreRejected() {
		assertEquals(1, validator.validate(request(0)).size());
		assertEquals(1, validator.validate(request(7)).size());
	}

	private static CreateReservationRequest request(int hours) {
		return new CreateReservationRequest(1L, "Anna", Instant.parse("2026-12-01T10:00:00Z"), hours);
	}
}
