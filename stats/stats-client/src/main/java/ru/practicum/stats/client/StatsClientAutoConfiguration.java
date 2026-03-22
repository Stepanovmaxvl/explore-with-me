package ru.practicum.stats.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties(StatsClientProperties.class)
public class StatsClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public StatsClient statsClient(RestTemplateBuilder builder, StatsClientProperties properties) {
		RestTemplate restTemplate = builder.build();
		return new RestTemplateStatsClient(restTemplate, properties.getUrl());
	}
}
