package com.atlas.incident;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class IncidentControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	IncidentRepository repository;

	@BeforeEach
	void clean() {
		repository.deleteAll();
	}

	private ResultActions postIncident(String title, String severity, String symptom) throws Exception {
		String json = "{\"title\":\"" + title + "\",\"severity\":\"" + severity
				+ "\",\"symptom\":\"" + symptom + "\"}";
		return mockMvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	@Test
	void createIncident_returns201_withBody() throws Exception {
		postIncident("DB 연결 실패", "HIGH", "502")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.severity").value("HIGH"))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void createIncident_blankTitle_returns400() throws Exception {
		postIncident("", "HIGH", "502").andExpect(status().isBadRequest());
	}

	@Test
	void createIncident_titleOver200_returns400() throws Exception {
		postIncident("a".repeat(201), "HIGH", "502").andExpect(status().isBadRequest());
	}

	@Test
	void createIncident_invalidSeverity_returns400() throws Exception {
		postIncident("제목", "SEVERE", "502").andExpect(status().isBadRequest());
	}

	@Test
	void getIncident_unknownId_returns404() throws Exception {
		mockMvc.perform(get("/api/incidents/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void getIncident_existing_returns200() throws Exception {
		String body = postIncident("조회용", "LOW", "증상").andReturn().getResponse().getContentAsString();
		String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");
		mockMvc.perform(get("/api/incidents/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("조회용"));
	}

	@Test
	void list_returnsNewestFirst() throws Exception {
		postIncident("먼저", "LOW", "a");
		Thread.sleep(5);
		postIncident("나중", "LOW", "b");
		mockMvc.perform(get("/api/incidents"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("나중"));
	}

	@Test
	void search_blankKeyword_returnsAll() throws Exception {
		postIncident("하나", "LOW", "a");
		postIncident("둘", "LOW", "b");
		mockMvc.perform(get("/api/incidents").param("keyword", " "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void search_koreanAndCase_matches() throws Exception {
		postIncident("DB 연결 실패", "HIGH", "502");
		postIncident("Nginx 502", "MEDIUM", "게이트웨이 오류");
		mockMvc.perform(get("/api/incidents").param("keyword", "db"))
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(get("/api/incidents").param("keyword", "연결"))
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(get("/api/incidents").param("keyword", "NGINX"))
				.andExpect(jsonPath("$.length()").value(1));
	}
}
