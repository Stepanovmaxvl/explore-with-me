package ru.practicum.stats.client;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatsClient {

	void hit(EndpointHitDto hit);

	List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
