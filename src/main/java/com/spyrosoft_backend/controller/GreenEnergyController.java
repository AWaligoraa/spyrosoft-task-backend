package com.spyrosoft_backend.controller;

import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.model.OptimalWindow;
import com.spyrosoft_backend.service.GreenEnergyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping("/generation-mix")
	public List<DayMix> threeDays() {
		return greenEnergyService.mixForThreeDays();
	}

	@GetMapping("/charging-window")
	public OptimalWindow window(
			@RequestParam @Min(value = 1, message = HOURS_MESSAGE) @Max(value = 6, message = HOURS_MESSAGE) int hours
	) {
		return greenEnergyService.bestWindow(hours);
	}
}
