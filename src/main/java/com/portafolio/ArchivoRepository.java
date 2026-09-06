package com.portafolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Integer> {
    List<Archivo> findByClaseId(Integer claseId);
}