package com.spyrosoft_backend.service;

import com.spyrosoft_backend.client.CarbonIntensityClient;
import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.model.FuelSource;
import com.spyrosoft_backend.model.GenerationSlot;
import com.spyrosoft_backend.time.DayRange;
import com.spyrosoft_backend.time.LondonDayRange;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GreenEnergyService {

	private static final Set<String> CLEAN_SOURCES = Set.of(
			"biomass", "nuclear", "hydro", "wind", "solar"
	);

	private final CarbonIntensityClient client;

	public GreenEnergyService(CarbonIntensityClient client) {
		this.client = client;
	}

	public List<DayMix> mixForThreeDays() {
		LocalDate today = LondonDayRange.today();
		List<DayMix> days = new ArrayList<>();
		for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
			LocalDate date = today.plusDays(dayOffset);
			DayRange range = LondonDayRange.of(date);
			List<GenerationSlot> slots = client.getGeneration(range.from(), range.to()).stream()
					.filter(slot -> !slot.from().isBefore(range.from()) && slot.from().isBefore(range.to()))
					.toList();
			days.add(aggregate(date, slots, range.expectedSlots()));
		}
		return days;
	}

	public DayMix aggregate(LocalDate date, List<GenerationSlot> slots, int expectedSlots) {
		int slotCount = slots.size();
		boolean complete = slotCount == expectedSlots;
		if (slotCount == 0) {
			return new DayMix(date, 0, complete, 0.0, List.of());
		}

		double cleanSum = 0.0;
		Map<String, Double> fuelTotals = new LinkedHashMap<>();

		for (GenerationSlot slot : slots) {
			double slotClean = 0.0;
			for (FuelSource source : slot.generationMix()) {
				fuelTotals.merge(source.fuel(), source.perc(), Double::sum);
				if (CLEAN_SOURCES.contains(source.fuel())) {
					slotClean += source.perc();
				}
			}
			cleanSum += slotClean;
		}

		List<FuelSource> sources = new ArrayList<>();
		for (Map.Entry<String, Double> entry : fuelTotals.entrySet()) {
			sources.add(new FuelSource(entry.getKey(), entry.getValue() / slotCount));
		}

		return new DayMix(date, slotCount, complete, cleanSum / slotCount, List.copyOf(sources));
	}
}
