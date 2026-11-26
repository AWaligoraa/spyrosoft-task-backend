package com.spyrosoft_backend.controller;

import com.spyrosoft_backend.model.DayMix;
import com.spyrosoft_backend.service.GreenEnergyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generation-mix")
public class GreenEnergyController {

	private final GreenEnergyService greenEnergyService;

	public GreenEnergyController(GreenEnergyService greenEnergyService) {
		this.greenEnergyService = greenEnergyService;
	}

	@GetMapping
	public List<DayMix> threeDays() {
		return greenEnergyService.mixForThreeDays();
	}
}
