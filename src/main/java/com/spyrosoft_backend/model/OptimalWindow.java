package com.spyrosoft_backend.model;

import java.time.Instant;
import java.util.List;

public record OptimalWindow(
		Instant start,
		Instant end,
		double cleanShare,
		List<Charger> availableChargers
) {
}
