package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

	private final StatsService statsService;

	@PostMapping("/hit")
	@ResponseStatus(HttpStatus.CREATED)
	public void hit(@Valid @RequestBody EndpointHitDto body) {
		statsService.recordHit(body);
	}

	@GetMapping("/stats")
	public List<ViewStatsDto> stats(
			@RequestParam String start,
			@RequestParam String end,
			@RequestParam(required = false) List<String> uris,
			@RequestParam(value = "unique", defaultValue = "false") boolean unique) {
		return statsService.getStats(start, end, uris, unique);
	}
}
