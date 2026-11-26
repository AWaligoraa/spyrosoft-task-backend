package com.spyrosoft_backend.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class LondonDayRange {

	public static final ZoneId LONDON = ZoneId.of("Europe/London");

	private LondonDayRange() {
	}

	public static DayRange of(LocalDate date) {
		ZonedDateTime start = date.atStartOfDay(LONDON);
		ZonedDateTime end = date.plusDays(1).atStartOfDay(LONDON);
		int expectedSlots = Math.toIntExact(Duration.between(start, end).toMinutes() / 30);
		return new DayRange(start.toInstant(), end.toInstant(), expectedSlots);
	}

	public static LocalDate today() {
		return LocalDate.now(LONDON);
	}
}
