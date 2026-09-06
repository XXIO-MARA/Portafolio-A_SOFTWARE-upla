package com.portafolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Integer> {
}