package com.spyrosoft_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReservationInThePastException extends RuntimeException {

	public ReservationInThePastException() {
		super("Reservation cannot start in the past");
	}
}
