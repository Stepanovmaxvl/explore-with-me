package ru.practicum.ewm.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.util.EwmDateTime;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatsFacade {

	private static final String APP = "ewm-main-service";
	private static final String STATS_START = "2000-01-01 00:00:00";
	private static final String STATS_END = "2100-12-31 23:59:59";

	private final StatsClient statsClient;

	public void recordHit(String uri, String ip) {
		EndpointHitDto dto = new EndpointHitDto();
		dto.setApp(APP);
		dto.setUri(uri);
		dto.setIp(ip);
		dto.setTimestamp(EwmDateTime.format(LocalDateTime.now()));
		statsClient.hit(dto);
	}

	public Map<Long, Long> getViewsByEventIds(List<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<String> uris = eventIds.stream().map(id -> "/events/" + id).toList();
		List<ViewStatsDto> stats = statsClient.getStats(STATS_START, STATS_END, uris, true);
		Map<String, Long> byUri = stats.stream()
				.collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, (a, b) -> a));
		Map<Long, Long> result = new HashMap<>();
		for (Long id : eventIds) {
			String uri = "/events/" + id;
			result.put(id, byUri.getOrDefault(uri, 0L));
		}
		return result;
	}

	public long getViewsForEvent(Long eventId) {
		return getViewsByEventIds(List.of(eventId)).getOrDefault(eventId, 0L);
	}
}
