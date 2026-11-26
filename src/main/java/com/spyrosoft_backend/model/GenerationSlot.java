package com.spyrosoft_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record GenerationSlot(
		Instant from,
		Instant to,
		@JsonProperty("generationmix") List<FuelSource> generationMix
) {
}
