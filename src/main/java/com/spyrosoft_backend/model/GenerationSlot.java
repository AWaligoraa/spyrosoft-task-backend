package com.spyrosoft_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GenerationSlot(
		Instant from,
		Instant to,
		List<FuelSource> generationMix
) {
	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

	@JsonCreator
	public GenerationSlot(
			@JsonProperty("from") String from,
			@JsonProperty("to") String to,
			@JsonProperty("generationmix") List<FuelSource> generationMix
	) {
		this(
				LocalDateTime.parse(from, TIMESTAMP).toInstant(ZoneOffset.UTC),
				LocalDateTime.parse(to, TIMESTAMP).toInstant(ZoneOffset.UTC),
				generationMix
		);
	}
}
