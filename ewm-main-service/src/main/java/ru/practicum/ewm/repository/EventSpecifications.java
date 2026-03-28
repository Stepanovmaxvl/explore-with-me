package ru.practicum.ewm.repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecifications {

	private EventSpecifications() {
	}

	public static Specification<Event> initiatorIn(List<Long> userIds) {
		return (root, query, cb) -> {
			if (userIds == null || userIds.isEmpty()) {
				return cb.conjunction();
			}
			return root.get("initiator").get("id").in(userIds);
		};
	}

	public static Specification<Event> stateIn(List<EventState> states) {
		return (root, query, cb) -> {
			if (states == null || states.isEmpty()) {
				return cb.conjunction();
			}
			return root.get("state").in(states);
		};
	}

	public static Specification<Event> categoryIn(List<Long> categoryIds) {
		return (root, query, cb) -> {
			if (categoryIds == null || categoryIds.isEmpty()) {
				return cb.conjunction();
			}
			return root.get("category").get("id").in(categoryIds);
		};
	}

	public static Specification<Event> eventDateFrom(LocalDateTime rangeStart) {
		return (root, query, cb) -> {
			if (rangeStart == null) {
				return cb.conjunction();
			}
			return cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
		};
	}

	public static Specification<Event> eventDateTo(LocalDateTime rangeEnd) {
		return (root, query, cb) -> {
			if (rangeEnd == null) {
				return cb.conjunction();
			}
			return cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
		};
	}

	public static Specification<Event> stateIs(EventState state) {
		return (root, query, cb) -> cb.equal(root.get("state"), state);
	}

	public static Specification<Event> textSearch(String text) {
		return (root, query, cb) -> {
			if (text == null || text.isBlank()) {
				return cb.conjunction();
			}
			String pattern = "%" + text.toLowerCase() + "%";
			return cb.or(
					cb.like(cb.lower(root.get("annotation")), pattern),
					cb.like(cb.lower(root.get("description")), pattern)
			);
		};
	}

	public static Specification<Event> paidEquals(Boolean paid) {
		return (root, query, cb) -> {
			if (paid == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("paid"), paid);
		};
	}

	public static Specification<Event> onlyAvailable() {
		return (root, query, cb) -> {
			Subquery<Long> confirmed = query.subquery(Long.class);
			Root<ParticipationRequest> pr = confirmed.from(ParticipationRequest.class);
			confirmed.select(cb.count(pr));
			confirmed.where(
					cb.equal(pr.get("event").get("id"), root.get("id")),
					cb.equal(pr.get("status"), RequestStatus.CONFIRMED)
			);
			Predicate unlimited = cb.equal(root.get("participantLimit"), 0);
			Predicate hasSlots = cb.gt(root.get("participantLimit"), confirmed);
			return cb.or(unlimited, hasSlots);
		};
	}

	public static Specification<Event> publicDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		return (root, query, cb) -> {
			if (rangeStart == null && rangeEnd == null) {
				return cb.greaterThan(root.get("eventDate"), LocalDateTime.now());
			}
			List<Predicate> parts = new ArrayList<>();
			if (rangeStart != null) {
				parts.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
			}
			if (rangeEnd != null) {
				parts.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
			}
			if (parts.isEmpty()) {
				return cb.conjunction();
			}
			return cb.and(parts.toArray(Predicate[]::new));
		};
	}
}
