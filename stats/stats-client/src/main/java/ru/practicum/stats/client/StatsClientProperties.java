package ru.practicum.stats.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stats-server")
public class StatsClientProperties {

	/**
	 * Базовый URL сервиса статистики (без завершающего слэша).
	 */
	private String url = "http://localhost:9090";
}
