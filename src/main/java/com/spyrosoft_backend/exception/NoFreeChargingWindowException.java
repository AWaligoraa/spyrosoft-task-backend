package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NoFreeChargingWindowException extends RuntimeException {

	public NoFreeChargingWindowException(boolean citySelected) {
		super(citySelected ? "No free charging window in that city" : "No free charging window");
	}
}
