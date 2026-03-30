package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

	private final CommentService commentService;

	@GetMapping
	public List<CommentDto> getComments(
			@PathVariable Long eventId,
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "10") int size) {
		return commentService.getPublicEventComments(eventId, from, size);
	}
}
