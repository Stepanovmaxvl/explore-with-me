package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateCommentController {

	private final CommentService commentService;

	@PostMapping("/events/{eventId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public CommentDto addComment(
			@PathVariable Long userId,
			@PathVariable Long eventId,
			@Valid @RequestBody NewCommentDto body) {
		return commentService.addComment(userId, eventId, body);
	}

	@GetMapping("/comments")
	public List<CommentDto> getUserComments(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "10") int size) {
		return commentService.getUserComments(userId, from, size);
	}
}
