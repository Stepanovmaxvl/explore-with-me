package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final RowMapper<ViewStatsDto> VIEW_STATS_MAPPER = (rs, rowNum) -> new ViewStatsDto(
			rs.getString("app"),
			rs.getString("uri"),
			rs.getLong("hits")
	);

	private static final String ALL_URIS_COUNT_HITS = """
			SELECT app, uri, COUNT(*) AS hits FROM hits
			WHERE event_time >= :start AND event_time <= :end
			GROUP BY app, uri ORDER BY hits DESC""";

	private static final String ALL_URIS_COUNT_DISTINCT_IP = """
			SELECT app, uri, COUNT(DISTINCT ip) AS hits FROM hits
			WHERE event_time >= :start AND event_time <= :end
			GROUP BY app, uri ORDER BY hits DESC""";

	private static final String FILTERED_URIS_COUNT_HITS = """
			SELECT app, uri, COUNT(*) AS hits FROM hits
			WHERE event_time >= :start AND event_time <= :end AND uri IN (:uris)
			GROUP BY app, uri ORDER BY hits DESC""";

	private static final String FILTERED_URIS_COUNT_DISTINCT_IP = """
			SELECT app, uri, COUNT(DISTINCT ip) AS hits FROM hits
			WHERE event_time >= :start AND event_time <= :end AND uri IN (:uris)
			GROUP BY app, uri ORDER BY hits DESC""";

	private final HitRepository hitRepository;
	private final NamedParameterJdbcTemplate namedJdbc;

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

		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("start", startTime)
				.addValue("end", endTime);

		if (uris != null && !uris.isEmpty()) {
			params.addValue("uris", uris);
			if (unique) {
				return namedJdbc.query(FILTERED_URIS_COUNT_DISTINCT_IP, params, VIEW_STATS_MAPPER);
			}
			return namedJdbc.query(FILTERED_URIS_COUNT_HITS, params, VIEW_STATS_MAPPER);
		}
		if (unique) {
			return namedJdbc.query(ALL_URIS_COUNT_DISTINCT_IP, params, VIEW_STATS_MAPPER);
		}
		return namedJdbc.query(ALL_URIS_COUNT_HITS, params, VIEW_STATS_MAPPER);
	}

	private static LocalDateTime parseRangeBoundary(String value, String name) {
		try {
			return LocalDateTime.parse(value, FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(name + ": ожидается формат yyyy-MM-dd HH:mm:ss");
		}
	}
}
