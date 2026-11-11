package com.atlas.incident;

import java.time.Instant;

public record IncidentResponse(Long id, String title, Severity severity, String symptom,
		String cause, String resolution, Instant createdAt) {

	static IncidentResponse from(Incident i) {
		return new IncidentResponse(i.getId(), i.getTitle(), i.getSeverity(), i.getSymptom(),
				i.getCause(), i.getResolution(), i.getCreatedAt());
	}
}
