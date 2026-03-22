package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

	private Long id;

	private String app;

	private String uri;

	private String ip;

	/** Формат: {@code yyyy-MM-dd HH:mm:ss} */
	private String timestamp;
}
