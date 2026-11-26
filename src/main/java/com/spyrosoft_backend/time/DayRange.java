package com.spyrosoft_backend.time;

import java.time.Instant;

public record DayRange(Instant from, Instant to, int expectedSlots) {
}
