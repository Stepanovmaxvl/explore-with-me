package ru.practicum.stats.repository;

import ru.practicum.dto.ViewStatsDto;

/**
 * Проекция результата агрегирующих запросов (Spring Data маппит по геттерам).
 */
public interface ViewStatsProjection {

	String getApp();

	String getUri();

	Long getHits();

	default ViewStatsDto toDto() {
		return new ViewStatsDto(getApp(), getUri(), getHits());
	}
}
