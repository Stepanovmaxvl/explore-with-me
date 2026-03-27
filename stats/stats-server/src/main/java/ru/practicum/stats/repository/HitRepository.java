package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {

	@Query(value = """
			SELECT app AS app, uri AS uri, COUNT(*) AS hits
			FROM hits
			WHERE event_time >= :start AND event_time <= :end
			GROUP BY app, uri
			ORDER BY hits DESC
			""", nativeQuery = true)
	List<ViewStatsProjection> findViewStatsCountAllHits(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query(value = """
			SELECT app AS app, uri AS uri, COUNT(DISTINCT ip) AS hits
			FROM hits
			WHERE event_time >= :start AND event_time <= :end
			GROUP BY app, uri
			ORDER BY hits DESC
			""", nativeQuery = true)
	List<ViewStatsProjection> findViewStatsCountDistinctIp(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query(value = """
			SELECT app AS app, uri AS uri, COUNT(*) AS hits
			FROM hits
			WHERE event_time >= :start AND event_time <= :end AND uri IN (:uris)
			GROUP BY app, uri
			ORDER BY hits DESC
			""", nativeQuery = true)
	List<ViewStatsProjection> findViewStatsCountAllHitsForUris(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("uris") List<String> uris);

	@Query(value = """
			SELECT app AS app, uri AS uri, COUNT(DISTINCT ip) AS hits
			FROM hits
			WHERE event_time >= :start AND event_time <= :end AND uri IN (:uris)
			GROUP BY app, uri
			ORDER BY hits DESC
			""", nativeQuery = true)
	List<ViewStatsProjection> findViewStatsCountDistinctIpForUris(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("uris") List<String> uris);
}
