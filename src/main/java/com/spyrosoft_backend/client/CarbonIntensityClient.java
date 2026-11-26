package com.spyrosoft_backend.client;

import com.spyrosoft_backend.exception.ExternalApiException;
import com.spyrosoft_backend.model.GenerationResponse;
import com.spyrosoft_backend.model.GenerationSlot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CarbonIntensityClient {

	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
			.withZone(ZoneOffset.UTC);

	private final RestClient restClient;

	public CarbonIntensityClient(@Value("${carbon-intensity.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("Accept", "application/json")
				.build();
	}

	public List<GenerationSlot> getGeneration(Instant from, Instant to) {
		try {
			GenerationResponse response = restClient.get()
					.uri("/generation/" + TIMESTAMP.format(from) + "/" + TIMESTAMP.format(to))
					.retrieve()
					.body(GenerationResponse.class);

			if (response == null || response.data() == null) {
				throw new ExternalApiException("Received empty data from Carbon Intensity API");
			}

			return response.data();
		} catch (ExternalApiException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ExternalApiException("Failed to fetch generation data: " + exception.getMessage());
		}
	}
}
