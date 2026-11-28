package com.spyrosoft_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Charger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String location;
	private String city;

	protected Charger() {
	}

	public Charger(Long id, String name, String location, String city) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.city = city;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public String getCity() {
		return city;
	}
}
