package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.CommentModerationRequest;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

	private final CommentService commentService;

	@GetMapping
	public List<CommentDto> getForModeration(
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "10") int size) {
		return commentService.getCommentsForModeration(status, from, size);
	}

	@PatchMapping("/{commentId}")
	public CommentDto moderate(
			@PathVariable Long commentId,
			@Valid @RequestBody CommentModerationRequest body) {
		return commentService.moderate(commentId, body);
	}
}
