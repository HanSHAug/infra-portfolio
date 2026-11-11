package com.atlas.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentRequest(
		@NotBlank @Size(max = 200) String title,
		@NotNull Severity severity,
		@NotBlank String symptom,
		String cause,
		String resolution) {
}
