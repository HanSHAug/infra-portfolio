package com.atlas.incident;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

	private final IncidentService service;

	public IncidentController(IncidentService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public IncidentResponse create(@Valid @RequestBody IncidentRequest request) {
		return service.create(request);
	}

	@GetMapping
	public List<IncidentResponse> list(@RequestParam(required = false) String keyword) {
		return service.search(keyword);
	}

	@GetMapping("/{id}")
	public IncidentResponse get(@PathVariable Long id) {
		return service.get(id);
	}
}
