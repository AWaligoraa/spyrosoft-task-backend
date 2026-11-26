package com.spyrosoft_backend.model;

import java.time.LocalDate;
import java.util.List;

public record DayMix(
		LocalDate date,
		int slotCount,
		boolean complete,
		double cleanShare,
		List<FuelSource> sources
) {
}
