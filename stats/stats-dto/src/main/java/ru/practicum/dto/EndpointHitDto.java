package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

	private Long id;

	@NotBlank(message = "app обязателен")
	private String app;

	@NotBlank(message = "uri обязателен")
	private String uri;

	@NotBlank(message = "ip обязателен")
	private String ip;

	@NotBlank(message = "timestamp обязателен")
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
			message = "timestamp в формате yyyy-MM-dd HH:mm:ss")
	private String timestamp;
}
