package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	Page<Comment> findAllByEventIdAndStatusOrderByIdAsc(Long eventId, CommentStatus status, Pageable pageable);

	Page<Comment> findAllByAuthorIdOrderByIdAsc(Long authorId, Pageable pageable);

	Page<Comment> findAllByStatusOrderByIdAsc(CommentStatus status, Pageable pageable);
}
