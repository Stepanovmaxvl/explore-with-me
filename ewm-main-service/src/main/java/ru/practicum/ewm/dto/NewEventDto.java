package ru.practicum.ewm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

	@NotBlank
	@Size(min = 20, max = 2000)
	private String annotation;

	@NotNull
	private Long category;

	@NotBlank
	@Size(min = 20, max = 7000)
	private String description;

	@NotBlank
	private String eventDate;

	@NotNull
	@Valid
	private LocationDto location;

	private Boolean paid;

	@PositiveOrZero
	private Integer participantLimit;

	private Boolean requestModeration;

	@NotBlank
	@Size(min = 3, max = 120)
	private String title;
}
