package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ChargerAlreadyBookedException extends RuntimeException {

	public ChargerAlreadyBookedException() {
		super("This charger is already booked for that time");
	}
}
