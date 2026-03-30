package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.LocationDto;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;

public final class EwmDtoMapper {

	private EwmDtoMapper() {
	}

	public static CategoryDto toCategoryDto(Category c) {
		return CategoryDto.builder().id(c.getId()).name(c.getName()).build();
	}

	public static UserDto toUserDto(User u) {
		return UserDto.builder().id(u.getId()).email(u.getEmail()).name(u.getName()).build();
	}

	public static UserShortDto toUserShort(User u) {
		return UserShortDto.builder().id(u.getId()).name(u.getName()).build();
	}

	public static LocationDto toLocationDto(Location loc) {
		if (loc == null) {
			return null;
		}
		return LocationDto.builder().lat((float) loc.getLat()).lon((float) loc.getLon()).build();
	}

	public static EventShortDto toEventShort(Event e, long views, long confirmed) {
		return EventShortDto.builder()
				.id(e.getId())
				.annotation(e.getAnnotation())
				.category(toCategoryDto(e.getCategory()))
				.confirmedRequests(confirmed)
				.eventDate(e.getEventDate())
				.initiator(toUserShort(e.getInitiator()))
				.paid(e.isPaid())
				.title(e.getTitle())
				.views(views)
				.build();
	}

	public static EventFullDto toEventFull(Event e, long views, long confirmed) {
		return EventFullDto.builder()
				.id(e.getId())
				.annotation(e.getAnnotation())
				.category(toCategoryDto(e.getCategory()))
				.confirmedRequests(confirmed)
				.createdOn(e.getCreatedOn())
				.description(e.getDescription())
				.eventDate(e.getEventDate())
				.initiator(toUserShort(e.getInitiator()))
				.location(toLocationDto(e.getLocation()))
				.paid(e.isPaid())
				.participantLimit(e.getParticipantLimit())
				.publishedOn(e.getPublishedOn())
				.requestModeration(e.isRequestModeration())
				.state(e.getState().name())
				.title(e.getTitle())
				.views(views)
				.build();
	}

	public static ParticipationRequestDto toParticipationDto(ParticipationRequest r) {
		return ParticipationRequestDto.builder()
				.id(r.getId())
				.created(r.getCreated())
				.event(r.getEvent().getId())
				.requester(r.getRequester().getId())
				.status(r.getStatus().name())
				.build();
	}

	public static CommentDto toCommentDto(Comment c) {
		return CommentDto.builder()
				.id(c.getId())
				.eventId(c.getEvent().getId())
				.author(toUserShort(c.getAuthor()))
				.text(c.getText())
				.created(c.getCreated())
				.status(c.getStatus().name())
				.moderatorNote(c.getModeratorNote())
				.build();
	}
}
