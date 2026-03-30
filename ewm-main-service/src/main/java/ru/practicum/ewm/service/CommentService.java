package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentModerationRequest;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;

	@Transactional
	public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Comments are available only for published events");
		}
		Comment comment = Comment.builder()
				.text(dto.getText().trim())
				.created(LocalDateTime.now())
				.status(CommentStatus.PENDING)
				.event(event)
				.author(author)
				.build();
		commentRepository.save(comment);
		return EwmDtoMapper.toCommentDto(comment);
	}

	public List<CommentDto> getPublicEventComments(Long eventId, int from, int size) {
		ensurePaging(from, size);
		eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
		return commentRepository.findAllByEventIdAndStatusOrderByIdAsc(
						eventId, CommentStatus.APPROVED, PageRequest.of(from / size, size))
				.getContent()
				.stream()
				.map(EwmDtoMapper::toCommentDto)
				.toList();
	}

	public List<CommentDto> getUserComments(Long userId, int from, int size) {
		ensurePaging(from, size);
		userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
		return commentRepository.findAllByAuthorIdOrderByIdAsc(userId, PageRequest.of(from / size, size))
				.getContent()
				.stream()
				.map(EwmDtoMapper::toCommentDto)
				.toList();
	}

	public List<CommentDto> getCommentsForModeration(String status, int from, int size) {
		ensurePaging(from, size);
		CommentStatus targetStatus;
		if (status == null || status.isBlank()) {
			targetStatus = CommentStatus.PENDING;
		} else {
			try {
				targetStatus = CommentStatus.valueOf(status.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid status parameter");
			}
		}
		return commentRepository.findAllByStatusOrderByIdAsc(targetStatus, PageRequest.of(from / size, size))
				.getContent()
				.stream()
				.map(EwmDtoMapper::toCommentDto)
				.toList();
	}

	@Transactional
	public CommentDto moderate(Long commentId, CommentModerationRequest body) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
		if (comment.getStatus() != CommentStatus.PENDING) {
			throw new ConflictException("Only pending comments can be moderated");
		}
		CommentStatus targetStatus;
		try {
			targetStatus = CommentStatus.valueOf(body.getStatus().trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Invalid moderation status");
		}
		if (targetStatus != CommentStatus.APPROVED && targetStatus != CommentStatus.REJECTED) {
			throw new BadRequestException("Invalid moderation status");
		}
		comment.setStatus(targetStatus);
		comment.setModeratorNote(body.getModeratorNote());
		commentRepository.save(comment);
		return EwmDtoMapper.toCommentDto(comment);
	}

	private void ensurePaging(int from, int size) {
		if (from < 0) {
			throw new BadRequestException("Field: from. Error: must not be negative.");
		}
		if (size <= 0) {
			throw new BadRequestException("Field: size. Error: must be positive.");
		}
	}
}
