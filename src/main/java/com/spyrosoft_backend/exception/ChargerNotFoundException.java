package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChargerNotFoundException extends RuntimeException {

	public ChargerNotFoundException() {
		super("Charger was not found");
	}
}
