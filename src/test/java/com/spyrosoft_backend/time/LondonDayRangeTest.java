package com.spyrosoft_backend.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LondonDayRangeTest {

	@Test
	void winterDayIsMidnightToMidnightUtc() {
		DayRange range = LondonDayRange.of(LocalDate.of(2026, 1, 15));

		assertEquals(Instant.parse("2026-01-15T00:00:00Z"), range.from());
		assertEquals(Instant.parse("2026-01-16T00:00:00Z"), range.to());
		assertEquals(48, range.expectedSlots());
	}

	@Test
	void summerDayStartsAnHourEarlierInUtc() {
		DayRange range = LondonDayRange.of(LocalDate.of(2026, 7, 15));

		assertEquals(Instant.parse("2026-07-14T23:00:00Z"), range.from());
		assertEquals(Instant.parse("2026-07-15T23:00:00Z"), range.to());
		assertEquals(48, range.expectedSlots());
	}

	@Test
	void clockChangeDaysDoNotHaveFortyEightSlots() {
		assertEquals(46, LondonDayRange.of(LocalDate.of(2026, 3, 29)).expectedSlots());
		assertEquals(50, LondonDayRange.of(LocalDate.of(2026, 10, 25)).expectedSlots());
	}
}
