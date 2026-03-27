package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.BusinessRuleException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

	private final ParticipationRequestRepository participationRepository;
	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
		ensureInitiator(userId, eventId);
		return participationRepository.findAllByEventIdOrderByIdAsc(eventId).stream()
				.map(EwmDtoMapper::toParticipationDto).collect(Collectors.toList());
	}

	public List<ParticipationRequestDto> getUserRequests(Long userId) {
		userService.ensureExists(userId);
		return participationRepository.findAllByRequesterIdWithEvent(userId).stream()
				.map(EwmDtoMapper::toParticipationDto).collect(Collectors.toList());
	}

	@Transactional
	public ParticipationRequestDto addRequest(Long userId, Long eventId) {
		User requester = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Participation is not available for this event");
		}
		if (event.getInitiator().getId().equals(userId)) {
			throw new ConflictException("Initiator cannot participate in own event");
		}
		if (participationRepository.existsByEventIdAndRequesterId(eventId, userId)) {
			throw new ConflictException("Duplicate participation request");
		}
		long limit = event.getParticipantLimit();
		long confirmed = participationRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
		if (limit > 0 && confirmed >= limit) {
			throw new ConflictException("The participant limit has been reached");
		}
		RequestStatus status = RequestStatus.PENDING;
		if (!event.isRequestModeration()) {
			status = RequestStatus.CONFIRMED;
		}
		ParticipationRequest pr = ParticipationRequest.builder()
				.created(LocalDateTime.now())
				.status(status)
				.event(event)
				.requester(requester)
				.build();
		participationRepository.save(pr);
		return EwmDtoMapper.toParticipationDto(pr);
	}

	@Transactional
	public EventRequestStatusUpdateResult changeStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest body) {
		Event event = ensureInitiator(userId, eventId);
		if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
			throw new BadRequestException("Confirmation not required for this event");
		}
		String action = body.getStatus();
		if (!"CONFIRMED".equals(action) && !"REJECTED".equals(action)) {
			throw new BadRequestException("Invalid status");
		}
		Map<Long, ParticipationRequest> byId = new HashMap<>();
		for (Long id : body.getRequestIds()) {
			ParticipationRequest r = participationRepository.findById(id).orElse(null);
			if (r == null || !r.getEvent().getId().equals(eventId)) {
				throw new BadRequestException("Request must have status PENDING");
			}
			byId.put(id, r);
		}
		for (ParticipationRequest r : byId.values()) {
			if (r.getStatus() != RequestStatus.PENDING) {
				throw new BadRequestException("Request must have status PENDING");
			}
		}
		List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
		List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();
		if ("REJECTED".equals(action)) {
			for (Long id : body.getRequestIds()) {
				ParticipationRequest r = byId.get(id);
				r.setStatus(RequestStatus.REJECTED);
				rejectedDtos.add(EwmDtoMapper.toParticipationDto(r));
			}
			participationRepository.saveAll(byId.values());
			return EventRequestStatusUpdateResult.builder()
					.confirmedRequests(confirmedDtos)
					.rejectedRequests(rejectedDtos)
					.build();
		}
		long limit = event.getParticipantLimit();
		long confirmedCount = participationRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
		for (Long id : body.getRequestIds()) {
			ParticipationRequest r = byId.get(id);
			if (limit > 0 && confirmedCount >= limit) {
				r.setStatus(RequestStatus.REJECTED);
				rejectedDtos.add(EwmDtoMapper.toParticipationDto(r));
			} else {
				r.setStatus(RequestStatus.CONFIRMED);
				confirmedCount++;
				confirmedDtos.add(EwmDtoMapper.toParticipationDto(r));
			}
		}
		participationRepository.saveAll(byId.values());
		if (limit > 0 && confirmedCount >= limit) {
			rejectRemainingPending(eventId);
		}
		return EventRequestStatusUpdateResult.builder()
				.confirmedRequests(confirmedDtos)
				.rejectedRequests(rejectedDtos)
				.build();
	}

	private void rejectRemainingPending(Long eventId) {
		List<ParticipationRequest> pending = participationRepository.findAllByEventIdAndStatusOrderByIdAsc(
				eventId, RequestStatus.PENDING);
		for (ParticipationRequest r : pending) {
			r.setStatus(RequestStatus.REJECTED);
		}
		participationRepository.saveAll(pending);
	}

	@Transactional
	public ParticipationRequestDto cancel(Long userId, Long requestId) {
		ParticipationRequest r = participationRepository.findByIdAndRequesterId(requestId, userId)
				.orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
		if (r.getStatus() != RequestStatus.PENDING) {
			throw new BusinessRuleException("FORBIDDEN", "Only pending requests can be cancelled");
		}
		r.setStatus(RequestStatus.CANCELED);
		participationRepository.save(r);
		return EwmDtoMapper.toParticipationDto(r);
	}

	private Event ensureInitiator(Long userId, Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}
		return event;
	}
}
