package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.UpdateEventUserRequest;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateUserEventController {

	private final EventService eventService;
	private final ParticipationService participationService;

	@GetMapping("/events")
	public List<EventShortDto> getUserEvents(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "10") int size) {
		return eventService.getUserEvents(userId, from, size);
	}

	@PostMapping("/events")
	@ResponseStatus(HttpStatus.CREATED)
	public EventFullDto addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto dto) {
		return eventService.create(userId, dto);
	}

	@GetMapping("/events/{eventId}")
	public EventFullDto getUserEvent(@PathVariable Long userId, @PathVariable Long eventId) {
		return eventService.getUserEvent(userId, eventId);
	}

	@PatchMapping("/events/{eventId}")
	public EventFullDto updateEvent(
			@PathVariable Long userId,
			@PathVariable Long eventId,
			@Valid @RequestBody UpdateEventUserRequest dto) {
		return eventService.updateUserEvent(userId, eventId, dto);
	}

	@GetMapping("/events/{eventId}/requests")
	public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
		return participationService.getEventRequests(userId, eventId);
	}

	@PatchMapping("/events/{eventId}/requests")
	public EventRequestStatusUpdateResult changeRequests(
			@PathVariable Long userId,
			@PathVariable Long eventId,
			@Valid @RequestBody EventRequestStatusUpdateRequest body) {
		return participationService.changeStatus(userId, eventId, body);
	}
}
