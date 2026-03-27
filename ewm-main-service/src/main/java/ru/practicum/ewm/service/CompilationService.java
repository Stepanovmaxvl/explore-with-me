package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.stats.StatsFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

	private final CompilationRepository compilationRepository;
	private final EventRepository eventRepository;
	private final StatsFacade statsFacade;

	public List<CompilationDto> getPublic(Boolean pinned, int from, int size) {
		var pageable = PageRequest.of(from / size, size);
		var page = pinned == null
				? compilationRepository.findAllByOrderByPinnedDescIdAsc(pageable)
				: compilationRepository.findAllByPinnedOrderByIdAsc(pinned, pageable);
		return page.getContent().stream().map(c -> toDto(c, true)).collect(Collectors.toList());
	}

	public CompilationDto getPublicById(Long id) {
		Compilation c = compilationRepository.findByIdWithEvents(id)
				.orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
		return toDto(c, true);
	}

	@Transactional
	public CompilationDto create(NewCompilationDto dto) {
		boolean pin = dto.getPinned() != null && dto.getPinned();
		Compilation c = Compilation.builder().title(dto.getTitle()).pinned(pin).build();
		if (dto.getEvents() != null) {
			for (Long eid : dto.getEvents()) {
				Event e = eventRepository.findById(eid)
						.orElseThrow(() -> new NotFoundException("Event with id=" + eid + " was not found"));
				c.getEvents().add(e);
			}
		}
		compilationRepository.save(c);
		return toDto(compilationRepository.findByIdWithEvents(c.getId()).orElse(c), false);
	}

	@Transactional
	public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
		Compilation c = compilationRepository.findByIdWithEvents(compId)
				.orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
		if (dto.getTitle() != null) {
			c.setTitle(dto.getTitle());
		}
		if (dto.getPinned() != null) {
			c.setPinned(dto.getPinned());
		}
		if (dto.getEvents() != null) {
			c.getEvents().clear();
			for (Long eid : dto.getEvents()) {
				Event e = eventRepository.findById(eid)
						.orElseThrow(() -> new NotFoundException("Event with id=" + eid + " was not found"));
				c.getEvents().add(e);
			}
		}
		compilationRepository.save(c);
		return toDto(compilationRepository.findByIdWithEvents(compId).orElse(c), false);
	}

	@Transactional
	public void delete(Long compId) {
		if (!compilationRepository.existsById(compId)) {
			throw new NotFoundException("Compilation with id=" + compId + " was not found");
		}
		compilationRepository.deleteById(compId);
	}

	private CompilationDto toDto(Compilation c, boolean publicOnly) {
		List<Event> events = c.getEvents().stream()
				.filter(e -> !publicOnly || e.getState() == EventState.PUBLISHED)
				.collect(Collectors.toList());
		List<Long> ids = events.stream().map(Event::getId).toList();
		var views = statsFacade.getViewsByEventIds(ids);
		List<EventShortDto> shorts = new ArrayList<>();
		for (Event e : events) {
			long v = views.getOrDefault(e.getId(), 0L);
			long conf = eventRepository.countConfirmedByEventId(e.getId());
			shorts.add(EwmDtoMapper.toEventShort(e, v, conf));
		}
		return CompilationDto.builder()
				.id(c.getId())
				.title(c.getTitle())
				.pinned(c.isPinned())
				.events(shorts)
				.build();
	}
}
