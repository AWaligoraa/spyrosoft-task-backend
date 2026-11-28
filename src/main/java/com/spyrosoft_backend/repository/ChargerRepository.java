package com.spyrosoft_backend.repository;

import com.spyrosoft_backend.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {

	List<Charger> findByCity(String city);
}
