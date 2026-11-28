package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoChargerInCityException extends RuntimeException {

	public NoChargerInCityException() {
		super("No charger in that city");
	}
}
