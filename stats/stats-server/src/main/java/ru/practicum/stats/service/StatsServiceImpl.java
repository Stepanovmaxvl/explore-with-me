package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

	private final HitRepository hitRepository;
	private final JdbcTemplate jdbcTemplate;

	@Override
	@Transactional
	public void recordHit(EndpointHitDto dto) {
		validate(dto);
		Hit hit = new Hit();
		hit.setApp(dto.getApp());
		hit.setUri(dto.getUri());
		hit.setIp(dto.getIp());
		hit.setEventTime(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
		hitRepository.save(hit);
	}

	@Override
	public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
		LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
		LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("start позже end");
		}
		String countExpr = unique ? "COUNT(DISTINCT ip)" : "COUNT(*)";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT app, uri, ").append(countExpr).append(" AS hits FROM hits ");
		sql.append("WHERE event_time >= ? AND event_time <= ? ");
		List<Object> args = new ArrayList<>();
		args.add(startTime);
		args.add(endTime);
		if (uris != null && !uris.isEmpty()) {
			sql.append("AND uri IN (");
			sql.append(String.join(",", Collections.nCopies(uris.size(), "?")));
			sql.append(") ");
			args.addAll(uris);
		}
		sql.append("GROUP BY app, uri ORDER BY hits DESC");
		return jdbcTemplate.query(sql.toString(), VIEW_STATS_MAPPER, args.toArray());
	}

	private void validate(EndpointHitDto dto) {
		if (dto.getApp() == null || dto.getApp().isBlank()) {
			throw new IllegalArgumentException("app обязателен");
		}
		if (dto.getUri() == null || dto.getUri().isBlank()) {
			throw new IllegalArgumentException("uri обязателен");
		}
		if (dto.getIp() == null || dto.getIp().isBlank()) {
			throw new IllegalArgumentException("ip обязателен");
		}
		if (dto.getTimestamp() == null || dto.getTimestamp().isBlank()) {
			throw new IllegalArgumentException("timestamp обязателен");
		}
	}
}
