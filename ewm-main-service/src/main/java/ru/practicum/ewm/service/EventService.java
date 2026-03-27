package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.BusinessRuleException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.EventSpecifications;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.stats.StatsFacade;
import ru.practicum.ewm.util.EwmDateTime;

import java.time.format.DateTimeParseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final StatsFacade statsFacade;

	@Transactional
	public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (dto.getAnnotation() != null) {
			event.setAnnotation(dto.getAnnotation());
		}
		if (dto.getCategory() != null) {
			Category cat = categoryRepository.findById(dto.getCategory())
					.orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));
			event.setCategory(cat);
		}
		if (dto.getDescription() != null) {
			event.setDescription(dto.getDescription());
		}
		if (dto.getEventDate() != null) {
			LocalDateTime newDate = EwmDateTime.parse(dto.getEventDate());
			if (!newDate.isAfter(LocalDateTime.now())) {
				throw new BadRequestException(
						"Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
								+ dto.getEventDate());
			}
			event.setEventDate(newDate);
		}
		if (dto.getLocation() != null) {
			event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
		}
		if (dto.getPaid() != null) {
			event.setPaid(dto.getPaid());
		}
		if (dto.getParticipantLimit() != null) {
			event.setParticipantLimit(dto.getParticipantLimit());
		}
		if (dto.getRequestModeration() != null) {
			event.setRequestModeration(dto.getRequestModeration());
		}
		if (dto.getTitle() != null) {
			event.setTitle(dto.getTitle());
		}
		if (dto.getStateAction() != null) {
			applyAdminStateAction(event, dto.getStateAction());
		}
		eventRepository.save(event);
		return toFull(event);
	}

	private void applyAdminStateAction(Event event, String action) {
		if ("PUBLISH_EVENT".equals(action)) {
			if (event.getState() != EventState.PENDING) {
				throw new BusinessRuleException("FORBIDDEN",
						"Cannot publish the event because it's not in the right state: " + event.getState());
			}
			LocalDateTime publishTime = LocalDateTime.now();
			if (!event.getEventDate().isAfter(publishTime.plusHours(1))) {
				throw new BusinessRuleException("FORBIDDEN",
						"Event date must be at least one hour after publication");
			}
			event.setState(EventState.PUBLISHED);
			event.setPublishedOn(publishTime);
		} else if ("REJECT_EVENT".equals(action)) {
			if (event.getState() != EventState.PENDING) {
				throw new BusinessRuleException("FORBIDDEN",
						"Cannot reject the event because it's not in the right state: " + event.getState());
			}
			event.setState(EventState.CANCELED);
		}
	}

	public List<EventFullDto> searchAdmin(List<Long> users, List<String> states, List<Long> categories,
			String rangeStart, String rangeEnd, int from, int size) {
		List<EventState> stateEnums = parseStates(states);
		LocalDateTime rs = parseRangeDate(rangeStart, "rangeStart");
		LocalDateTime re = parseRangeDate(rangeEnd, "rangeEnd");
		Specification<Event> spec = Specification.where(EventSpecifications.initiatorIn(users))
				.and(EventSpecifications.stateIn(stateEnums))
				.and(EventSpecifications.categoryIn(categories))
				.and(EventSpecifications.eventDateFrom(rs))
				.and(EventSpecifications.eventDateTo(re));
		var page = eventRepository.findAll(spec, PageRequest.of(from / size, size, Sort.by("id").ascending()));
		return page.getContent().stream().map(this::toFull).collect(Collectors.toList());
	}

	public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
			String rangeStart, String rangeEnd, boolean onlyAvailable, String sort, int from, int size, String ip) {
		if (text != null && text.isBlank()) {
			throw new BadRequestException("Field: text. Error: size must be between 1 and 7000 if text is set.");
		}
		if (from < 0) {
			throw new BadRequestException("Field: from. Error: must not be negative.");
		}
		if (size <= 0) {
			throw new BadRequestException("Field: size. Error: must be positive.");
		}
		if (sort != null && !sort.isBlank() && !"EVENT_DATE".equals(sort) && !"VIEWS".equals(sort)) {
			throw new BadRequestException("Invalid sort parameter");
		}
		LocalDateTime rs = parseRangeDate(rangeStart, "rangeStart");
		LocalDateTime re = parseRangeDate(rangeEnd, "rangeEnd");
		if (rs != null && re != null && rs.isAfter(re)) {
			throw new BadRequestException("Field: rangeStart. Error: must not be after rangeEnd.");
		}
		statsFacade.recordHit("/events", ip);
		Specification<Event> spec = Specification.where(EventSpecifications.stateIs(EventState.PUBLISHED))
				.and(EventSpecifications.textSearch(text))
				.and(EventSpecifications.categoryIn(categories))
				.and(EventSpecifications.paidEquals(paid))
				.and(EventSpecifications.publicDateRange(rs, re));
		if (onlyAvailable) {
			spec = spec.and(EventSpecifications.onlyAvailable());
		}
		String sortMode = sort == null ? "EVENT_DATE" : sort;
		if ("VIEWS".equals(sortMode)) {
			List<Event> all = eventRepository.findAll(spec);
			List<Long> ids = all.stream().map(Event::getId).toList();
			Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);
			Comparator<Event> cmp = Comparator.<Event, Long>comparing(e -> views.getOrDefault(e.getId(), 0L)).reversed()
					.thenComparing(Event::getId);
			List<Event> sorted = all.stream().sorted(cmp).toList();
			int to = Math.min(from + size, sorted.size());
			if (from >= sorted.size()) {
				return List.of();
			}
			sorted = sorted.subList(from, to);
			return sorted.stream().map(this::toShort).collect(Collectors.toList());
		}
		Sort s = Sort.by("eventDate").ascending();
		var page = eventRepository.findAll(spec, PageRequest.of(from / size, size, s));
		return page.getContent().stream().map(this::toShort).collect(Collectors.toList());
	}

	public EventFullDto getPublicById(Long id, String ip) {
		Event event = eventRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
		if (event.getState() != EventState.PUBLISHED) {
			throw new NotFoundException("Event with id=" + id + " was not found");
		}
		statsFacade.recordHit("/events/" + id, ip);
		return toFull(event);
	}

	public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
		ensureUser(userId);
		return eventRepository.findAllByInitiatorIdOrderByIdAsc(userId, PageRequest.of(from / size, size))
				.getContent().stream().map(this::toShort).collect(Collectors.toList());
	}

	@Transactional
	public EventFullDto create(Long userId, NewEventDto dto) {
		User initiator = ensureUser(userId);
		Category category = categoryRepository.findById(dto.getCategory())
				.orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));
		LocalDateTime eventDate = parseEventDateRequired(dto.getEventDate());
		validateEventDateNotTooSoon(eventDate);
		int pl = dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit();
		boolean rm = dto.getRequestModeration() == null || dto.getRequestModeration();
		boolean paid = dto.getPaid() != null && dto.getPaid();
		Event event = Event.builder()
				.annotation(dto.getAnnotation())
				.description(dto.getDescription())
				.eventDate(eventDate)
				.createdOn(LocalDateTime.now())
				.location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
				.paid(paid)
				.participantLimit(pl)
				.requestModeration(rm)
				.state(EventState.PENDING)
				.title(dto.getTitle())
				.category(category)
				.initiator(initiator)
				.build();
		eventRepository.save(event);
		return toFull(event);
	}

	public EventFullDto getUserEvent(Long userId, Long eventId) {
		ensureUser(userId);
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}
		return toFull(event);
	}

	@Transactional
	public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
		ensureUser(userId);
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (!event.getInitiator().getId().equals(userId)) {
			throw new NotFoundException("Event with id=" + eventId + " was not found");
		}
		if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
			throw new BusinessRuleException("FORBIDDEN", "Only pending or canceled events can be changed");
		}
		if (dto.getAnnotation() != null) {
			event.setAnnotation(dto.getAnnotation());
		}
		if (dto.getCategory() != null) {
			Category cat = categoryRepository.findById(dto.getCategory())
					.orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));
			event.setCategory(cat);
		}
		if (dto.getDescription() != null) {
			event.setDescription(dto.getDescription());
		}
		if (dto.getEventDate() != null && !dto.getEventDate().isBlank()) {
			LocalDateTime ed = parseEventDateRequired(dto.getEventDate());
			validateEventDateNotTooSoon(ed);
			event.setEventDate(ed);
		}
		if (dto.getLocation() != null) {
			event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
		}
		if (dto.getPaid() != null) {
			event.setPaid(dto.getPaid());
		}
		if (dto.getParticipantLimit() != null) {
			event.setParticipantLimit(dto.getParticipantLimit());
		}
		if (dto.getRequestModeration() != null) {
			event.setRequestModeration(dto.getRequestModeration());
		}
		if (dto.getTitle() != null) {
			event.setTitle(dto.getTitle());
		}
		if (dto.getStateAction() != null) {
			applyUserStateAction(event, dto.getStateAction());
		}
		eventRepository.save(event);
		return toFull(event);
	}

	private void applyUserStateAction(Event event, String action) {
		if ("CANCEL_REVIEW".equals(action)) {
			if (event.getState() != EventState.PENDING) {
				throw new BusinessRuleException("FORBIDDEN", "Only pending or canceled events can be changed");
			}
			event.setState(EventState.CANCELED);
		} else if ("SEND_TO_REVIEW".equals(action)) {
			if (event.getState() != EventState.CANCELED) {
				throw new BusinessRuleException("FORBIDDEN", "Only pending or canceled events can be changed");
			}
			event.setState(EventState.PENDING);
		}
	}

	private void validateEventDateNotTooSoon(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
			throw new BadRequestException(
					"Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
							+ EwmDateTime.format(eventDate));
		}
	}

	private LocalDateTime parseEventDateRequired(String value) {
		try {
			LocalDateTime dt = EwmDateTime.parse(value);
			if (dt == null) {
				throw new BadRequestException("Field: eventDate. Error: must not be blank.");
			}
			return dt;
		} catch (DateTimeParseException e) {
			throw new BadRequestException("Field: eventDate. Error: invalid date format.");
		}
	}

	private LocalDateTime parseRangeDate(String value, String field) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return LocalDateTime.parse(value.trim(), EwmDateTime.API_DATE_TIME);
		} catch (DateTimeParseException e) {
			throw new BadRequestException("Field: " + field + ". Error: invalid date format.");
		}
	}

	private User ensureUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
	}

	private List<EventState> parseStates(List<String> states) {
		if (states == null || states.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			return states.stream().map(EventState::valueOf).collect(Collectors.toList());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Invalid state parameter");
		}
	}

	private EventFullDto toFull(Event e) {
		long confirmed = eventRepository.countConfirmedByEventId(e.getId());
		long views = statsFacade.getViewsForEvent(e.getId());
		return EwmDtoMapper.toEventFull(e, views, confirmed);
	}

	private EventShortDto toShort(Event e) {
		long confirmed = eventRepository.countConfirmedByEventId(e.getId());
		long views = statsFacade.getViewsForEvent(e.getId());
		return EwmDtoMapper.toEventShort(e, views, confirmed);
	}
}
