package com.spyrosoft_backend.service;

import com.spyrosoft_backend.client.CarbonIntensityClient;
import com.spyrosoft_backend.exception.NoChargingWindowException;
import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.model.FuelSource;
import com.spyrosoft_backend.model.GenerationSlot;
import com.spyrosoft_backend.model.OptimalWindow;
import com.spyrosoft_backend.time.DayRange;
import com.spyrosoft_backend.time.LondonDayRange;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
			days.add(aggregate(date, slotsFor(date), LondonDayRange.of(date).expectedSlots()));
		}
		return days;
	}

	public OptimalWindow bestWindow(int hours) {
		LocalDate today = LondonDayRange.today();
		List<GenerationSlot> slots = new ArrayList<>();
		slots.addAll(slotsFor(today));
		slots.addAll(slotsFor(today.plusDays(1)));
		slots.sort(Comparator.comparing(GenerationSlot::from));
		return findBestWindow(slots, hours);
	}

	public OptimalWindow findBestWindow(List<GenerationSlot> slots, int hours) {
		int windowSize = hours * 2;
		double bestShare = -1;
		int bestStart = -1;

		for (int start = 0; start <= slots.size() - windowSize; start++) {
			if (!contiguous(slots, start, windowSize)) {
				continue;
			}
			double share = averageCleanShare(slots, start, windowSize);
			if (share > bestShare) {
				bestShare = share;
				bestStart = start;
			}
		}

		if (bestStart < 0) {
			throw new NoChargingWindowException();
		}

		GenerationSlot first = slots.get(bestStart);
		GenerationSlot last = slots.get(bestStart + windowSize - 1);
		return new OptimalWindow(first.from(), last.to(), bestShare);
	}

	private List<GenerationSlot> slotsFor(LocalDate date) {
		DayRange range = LondonDayRange.of(date);
		return client.getGeneration(range.from(), range.to()).stream()
				.filter(slot -> !slot.from().isBefore(range.from()) && slot.from().isBefore(range.to()))
				.toList();
	}

	private double averageCleanShare(List<GenerationSlot> slots, int start, int windowSize) {
		double sum = 0;
		for (int index = start; index < start + windowSize; index++) {
			sum += cleanPercent(slots.get(index));
		}
		return sum / windowSize;
	}

	private double cleanPercent(GenerationSlot slot) {
		double sum = 0;
		for (FuelSource source : slot.generationMix()) {
			if (CLEAN_SOURCES.contains(source.fuel())) {
				sum += source.perc();
			}
		}
		return sum;
	}

	private boolean contiguous(List<GenerationSlot> slots, int start, int windowSize) {
		for (int index = start; index < start + windowSize - 1; index++) {
			Duration gap = Duration.between(slots.get(index).from(), slots.get(index + 1).from());
			if (!gap.equals(Duration.ofMinutes(30))) {
				return false;
			}
		}
		return true;
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
