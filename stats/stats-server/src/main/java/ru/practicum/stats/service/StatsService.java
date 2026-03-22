package ru.practicum.stats.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {

	void recordHit(EndpointHitDto dto);

	List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique);
}
