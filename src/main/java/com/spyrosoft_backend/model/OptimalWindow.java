package com.spyrosoft_backend.model;

import java.time.Instant;

public record OptimalWindow(
		Instant start,
		Instant end,
		double cleanShare
) {
}
