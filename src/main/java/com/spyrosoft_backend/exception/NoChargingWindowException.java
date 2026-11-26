package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NoChargingWindowException extends RuntimeException {

	public NoChargingWindowException() {
		super("Not enough forecast data to find a charging window");
	}
}
