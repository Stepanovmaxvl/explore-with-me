package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;
import ru.practicum.stats.repository.ViewStatsProjection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final HitRepository hitRepository;

	@Override
	@Transactional
	public void recordHit(EndpointHitDto dto) {
		LocalDateTime eventTime;
		try {
			eventTime = LocalDateTime.parse(dto.getTimestamp(), FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("timestamp: некорректная дата и время");
		}
		Hit hit = new Hit();
		hit.setApp(dto.getApp());
		hit.setUri(dto.getUri());
		hit.setIp(dto.getIp());
		hit.setEventTime(eventTime);
		hitRepository.save(hit);
	}

	@Override
	public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
		LocalDateTime startTime = parseRangeBoundary(start, "start");
		LocalDateTime endTime = parseRangeBoundary(end, "end");
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("start позже end");
		}

		if (uris != null && !uris.isEmpty()) {
			if (unique) {
				return toDtos(hitRepository.findViewStatsCountDistinctIpForUris(startTime, endTime, uris));
			}
			return toDtos(hitRepository.findViewStatsCountAllHitsForUris(startTime, endTime, uris));
		}
		if (unique) {
			return toDtos(hitRepository.findViewStatsCountDistinctIp(startTime, endTime));
		}
		return toDtos(hitRepository.findViewStatsCountAllHits(startTime, endTime));
	}

	private static List<ViewStatsDto> toDtos(List<ViewStatsProjection> rows) {
		return rows.stream().map(ViewStatsProjection::toDto).toList();
	}

	private static LocalDateTime parseRangeBoundary(String value, String name) {
		try {
			return LocalDateTime.parse(value, FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(name + ": ожидается формат yyyy-MM-dd HH:mm:ss");
		}
	}
}
