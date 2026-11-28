package com.spyrosoft_backend.service;

import com.spyrosoft_backend.client.CarbonIntensityClient;
import com.spyrosoft_backend.exception.NoChargerInCityException;
import com.spyrosoft_backend.exception.NoChargingWindowException;
import com.spyrosoft_backend.exception.NoFreeChargingWindowException;
import com.spyrosoft_backend.model.*;
import com.spyrosoft_backend.repository.ChargerRepository;
import com.spyrosoft_backend.repository.ReservationRepository;
import com.spyrosoft_backend.time.DayRange;
import com.spyrosoft_backend.time.LondonDayRange;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
	private final ChargerRepository chargerRepository;
	private final ReservationRepository reservationRepository;
	private final Clock clock;

	public GreenEnergyService(
			CarbonIntensityClient client,
			ChargerRepository chargerRepository,
			ReservationRepository reservationRepository,
			Clock clock
	) {
		this.client = client;
		this.chargerRepository = chargerRepository;
		this.reservationRepository = reservationRepository;
		this.clock = clock;
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
		return findBestWindow(todayAndTomorrow(), hours);
	}

	public OptimalWindow bestWindow(int hours, boolean bookable, String city) {
		if (!bookable) {
			return bestWindow(hours);
		}
		String selectedCity = city == null ? "" : city.trim();
		List<Charger> chargers = selectedCity.isEmpty()
				? chargerRepository.findAll()
				: chargerRepository.findByCity(selectedCity);
		if (!selectedCity.isEmpty() && chargers.isEmpty()) {
			throw new NoChargerInCityException();
		}
		List<Long> chargerIds = chargers.stream().map(Charger::getId).toList();
		List<Reservation> reservations = chargerIds.isEmpty()
				? List.of()
				: reservationRepository.findByCharger_IdIn(chargerIds);
		return findBookableWindow(
				todayAndTomorrow(),
				hours,
				chargers,
				reservations,
				clock.instant(),
				!selectedCity.isEmpty()
		);
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
		return new OptimalWindow(first.from(), last.to(), bestShare, List.of());
	}

	public OptimalWindow findBookableWindow(
			List<GenerationSlot> slots,
			int hours,
			List<Charger> chargers,
			List<Reservation> reservations,
			Instant now,
			boolean citySelected
	) {
		int windowSize = hours * 2;
		boolean anyContinuous = false;
		double bestShare = -1;
		int bestStart = -1;
		List<Charger> bestChargers = List.of();

		for (int start = 0; start <= slots.size() - windowSize; start++) {
			if (!contiguous(slots, start, windowSize)) {
				continue;
			}
			anyContinuous = true;
			GenerationSlot first = slots.get(start);
			GenerationSlot last = slots.get(start + windowSize - 1);
			if (first.from().isBefore(now)) {
				continue;
			}
			List<Charger> free = freeChargers(chargers, reservations, first.from(), last.to());
			if (free.isEmpty()) {
				continue;
			}
			double share = averageCleanShare(slots, start, windowSize);
			if (share > bestShare) {
				bestShare = share;
				bestStart = start;
				bestChargers = free;
			}
		}

		if (!anyContinuous) {
			throw new NoChargingWindowException();
		}
		if (bestStart < 0) {
			throw new NoFreeChargingWindowException(citySelected);
		}

		GenerationSlot first = slots.get(bestStart);
		GenerationSlot last = slots.get(bestStart + windowSize - 1);
		return new OptimalWindow(first.from(), last.to(), bestShare, bestChargers);
	}

	private List<GenerationSlot> todayAndTomorrow() {
		LocalDate today = LondonDayRange.today();
		List<GenerationSlot> slots = new ArrayList<>();
		slots.addAll(slotsFor(today));
		slots.addAll(slotsFor(today.plusDays(1)));
		slots.sort(Comparator.comparing(GenerationSlot::from));
		return slots;
	}

	private List<Charger> freeChargers(
			List<Charger> chargers,
			List<Reservation> reservations,
			Instant windowStart,
			Instant windowEnd
	) {
		List<Charger> free = new ArrayList<>();
		for (Charger charger : chargers) {
			boolean busy = reservations.stream().anyMatch(reservation ->
					reservation.getCharger().getId().equals(charger.getId())
							&& reservation.getStartsAt().isBefore(windowEnd)
							&& reservation.getEndsAt().isAfter(windowStart)
			);
			if (!busy) {
				free.add(charger);
			}
		}
		return free;
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

	public List<Charger> getChargers() {
		return chargerRepository.findAll();
	}
	
}
