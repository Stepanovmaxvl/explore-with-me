package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RestTemplateStatsClient implements StatsClient {

	private final RestTemplate restTemplate;
	private final String baseUrl;

	@Override
	public void hit(EndpointHitDto hit) {
		try {
			restTemplate.postForEntity(baseUrl + "/hit", hit, Void.class);
		} catch (Exception e) {
			log.warn("Не удалось отправить hit в stats-server: {}", e.getMessage());
		}
	}

	@Override
	public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/stats")
					.queryParam("start", start)
					.queryParam("end", end)
					.queryParam("unique", Boolean.TRUE.equals(unique));
			if (uris != null) {
				for (String uri : uris) {
					builder.queryParam("uris", uri);
				}
			}
			URI uri = builder.encode().build().toUri();
			ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
					uri,
					HttpMethod.GET,
					HttpEntity.EMPTY,
					new ParameterizedTypeReference<>() {
					});
			List<ViewStatsDto> body = response.getBody();
			return body != null ? body : Collections.emptyList();
		} catch (Exception e) {
			log.warn("Не удалось получить статистику: {}", e.getMessage());
			return Collections.emptyList();
		}
	}
}
