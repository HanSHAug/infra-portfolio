package com.atlas.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Incident {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Severity severity;

	@Column(nullable = false, columnDefinition = "text")
	private String symptom;

	@Column(columnDefinition = "text")
	private String cause;

	@Column(columnDefinition = "text")
	private String resolution;

	@Column(nullable = false)
	private Instant createdAt;

	protected Incident() {
	}

	public Incident(String title, Severity severity, String symptom, String cause, String resolution) {
		this.title = title;
		this.severity = severity;
		this.symptom = symptom;
		this.cause = cause;
		this.resolution = resolution;
		this.createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getTitle() { return title; }
	public Severity getSeverity() { return severity; }
	public String getSymptom() { return symptom; }
	public String getCause() { return cause; }
	public String getResolution() { return resolution; }
	public Instant getCreatedAt() { return createdAt; }
}
