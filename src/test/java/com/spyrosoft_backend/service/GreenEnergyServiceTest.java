package com.spyrosoft_backend.service;

import com.spyrosoft_backend.exception.NoChargingWindowException;
import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.model.FuelSource;
import com.spyrosoft_backend.model.GenerationSlot;
import com.spyrosoft_backend.model.OptimalWindow;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GreenEnergyServiceTest {

	private final GreenEnergyService service = new GreenEnergyService(null, null, null, null);
	private final LocalDate date = LocalDate.of(2026, 1, 15);

	@Test
	void missingFuelCountsAsZeroAndPartialDayIsNotDividedByAFullDay() {
		List<GenerationSlot> slots = List.of(
				slot("2026-01-15T00:00:00Z", "2026-01-15T00:30:00Z",
						source("wind", 50), source("gas", 30), source("solar", 20)),
				slot("2026-01-15T00:30:00Z", "2026-01-15T01:00:00Z",
						source("wind", 30), source("gas", 40), source("solar", 20), source("nuclear", 10))
		);

		DayMix day = service.aggregate(date, slots, 48);

		assertEquals(2, day.slotCount());
		assertFalse(day.complete());
		assertEquals(65, day.cleanShare(), 0.001);
		assertEquals(40, share(day, "wind"), 0.001);
		assertEquals(35, share(day, "gas"), 0.001);
		assertEquals(20, share(day, "solar"), 0.001);
		assertEquals(5, share(day, "nuclear"), 0.001);
	}

	@Test
	void windowCrossingMidnightBeatsCleanerLookingHoursInsideOneDay() {
		List<GenerationSlot> slots = List.of(
				cleanSlot("2026-01-15T23:00:00Z", 20),
				cleanSlot("2026-01-15T23:30:00Z", 80),
				cleanSlot("2026-01-16T00:00:00Z", 80),
				cleanSlot("2026-01-16T00:30:00Z", 20)
		);

		OptimalWindow window = service.findBestWindow(slots, 1);

		assertEquals(Instant.parse("2026-01-15T23:30:00Z"), window.start());
		assertEquals(Instant.parse("2026-01-16T00:30:00Z"), window.end());
		assertEquals(80, window.cleanShare(), 0.001);
	}

	@Test
	void gapBetweenSlotsIsNotTreatedAsOneContinuousCharge() {
		List<GenerationSlot> slots = List.of(
				cleanSlot("2026-01-15T00:00:00Z", 100),
				cleanSlot("2026-01-15T01:00:00Z", 100)
		);

		assertThrows(NoChargingWindowException.class, () -> service.findBestWindow(slots, 1));
	}

	@Test
	void fourHourWindowNeedsMoreThanThreeHoursOfSlots() {
		List<GenerationSlot> slots = List.of(
				cleanSlot("2026-01-15T00:00:00Z", 80),
				cleanSlot("2026-01-15T00:30:00Z", 80),
				cleanSlot("2026-01-15T01:00:00Z", 80),
				cleanSlot("2026-01-15T01:30:00Z", 80),
				cleanSlot("2026-01-15T02:00:00Z", 80),
				cleanSlot("2026-01-15T02:30:00Z", 80)
		);

		assertThrows(NoChargingWindowException.class, () -> service.findBestWindow(slots, 4));
	}

	private static GenerationSlot cleanSlot(String from, double cleanPercent) {
		Instant start = Instant.parse(from);
		return new GenerationSlot(start, start.plus(30, ChronoUnit.MINUTES), List.of(
				source("wind", cleanPercent),
				source("gas", 100 - cleanPercent)
		));
	}

	private static GenerationSlot slot(String from, String to, FuelSource... sources) {
		return new GenerationSlot(Instant.parse(from), Instant.parse(to), List.of(sources));
	}

	private static FuelSource source(String fuel, double percentage) {
		return new FuelSource(fuel, percentage);
	}

	private static double share(DayMix day, String fuel) {
		return day.sources().stream()
				.filter(source -> source.fuel().equals(fuel))
				.findFirst()
				.orElseThrow()
				.perc();
	}
}
