package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatsServerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void postHitThenGetStatsWithoutUris() throws Exception {
		EndpointHitDto hit = new EndpointHitDto(null, "ewm-main-service", "/events", "1.1.1.1", "2022-01-01 12:00:00");
		mockMvc.perform(post("/hit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(hit)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/stats")
						.param("start", "2020-05-05 00:00:00")
						.param("end", "2035-05-05 00:00:00")
						.param("unique", "false"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].hits").exists())
				.andExpect(jsonPath("$[0].app").value("ewm-main-service"));
	}
}
