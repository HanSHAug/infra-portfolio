package com.atlas.incident;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

	List<Incident> findAllByOrderByCreatedAtDescIdDesc();

	List<Incident> findByTitleContainingIgnoreCaseOrSymptomContainingIgnoreCaseOrderByCreatedAtDescIdDesc(
			String title, String symptom);
}
