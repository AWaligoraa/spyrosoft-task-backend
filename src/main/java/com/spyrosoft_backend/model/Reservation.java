package com.spyrosoft_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "charger_id", nullable = false)
	private Charger charger;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	protected Reservation() {
	}

	public Reservation(Charger charger, String userName, Instant startsAt, Instant endsAt) {
		this.charger = charger;
		this.userName = userName;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
	}

	public Long getId() {
		return id;
	}

	public Charger getCharger() {
		return charger;
	}

	public String getUserName() {
		return userName;
	}

	public Instant getStartsAt() {
		return startsAt;
	}

	public Instant getEndsAt() {
		return endsAt;
	}
}
