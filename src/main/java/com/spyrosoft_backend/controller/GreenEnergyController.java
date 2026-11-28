package com.spyrosoft_backend.controller;

import com.spyrosoft_backend.model.Charger;
import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.model.OptimalWindow;
import com.spyrosoft_backend.service.GreenEnergyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class GreenEnergyController {

	private static final String HOURS_MESSAGE = "Charging time must be between 1 and 6 hours";

	private final GreenEnergyService greenEnergyService;

	public GreenEnergyController(GreenEnergyService greenEnergyService) {
		this.greenEnergyService = greenEnergyService;
	}

	@Operation(summary = "Average generation mix and clean energy share for today, tomorrow and the day after (Europe/London days)")
	@GetMapping("/generation-mix")
	public List<DayMix> threeDays() {
		return greenEnergyService.mixForThreeDays();
	}

	@Operation(summary = "Cleanest continuous charging window for today and tomorrow (Europe/London days). bookable=true skips past times and occupied chargers, optionally in one city")
	@GetMapping("/charging-window")
	public OptimalWindow window(
			@RequestParam @Min(value = 1, message = HOURS_MESSAGE) @Max(value = 6, message = HOURS_MESSAGE) int hours,
			@RequestParam(defaultValue = "false") boolean bookable,
			@RequestParam(required = false) String city
	) {
		return greenEnergyService.bestWindow(hours, bookable, city);
	}

	@Operation(summary = "Chargers available for booking")
	@GetMapping("/chargers")
	public List<Charger> chargers() { return greenEnergyService.getChargers(); }


}
