package com.spyrosoft_backend.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateReservationRequest(
		@NotNull Long chargerId,
		@NotBlank String userName,
		@NotNull Instant startsAt,
		@NotNull @Min(1) @Max(6) Integer hours
) {
}
