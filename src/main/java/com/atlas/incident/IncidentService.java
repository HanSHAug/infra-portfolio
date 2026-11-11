package com.atlas.incident;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IncidentService {

	private final IncidentRepository repository;

	public IncidentService(IncidentRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public IncidentResponse create(IncidentRequest r) {
		Incident saved = repository.save(
				new Incident(r.title(), r.severity(), r.symptom(), r.cause(), r.resolution()));
		return IncidentResponse.from(saved);
	}

	public List<IncidentResponse> list() {
		return repository.findAllByOrderByCreatedAtDescIdDesc().stream().map(IncidentResponse::from).toList();
	}

	public IncidentResponse get(Long id) {
		return repository.findById(id).map(IncidentResponse::from)
				.orElseThrow(() -> new IncidentNotFoundException(id));
	}

	public List<IncidentResponse> search(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return list();
		}
		String k = keyword.trim();
		return repository
				.findByTitleContainingIgnoreCaseOrSymptomContainingIgnoreCaseOrderByCreatedAtDescIdDesc(k, k)
				.stream().map(IncidentResponse::from).toList();
	}
}
