package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

	Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);

	boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

	@Query("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = :status")
	long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

	List<ParticipationRequest> findAllByEventIdOrderByIdAsc(Long eventId);

	List<ParticipationRequest> findAllByRequesterIdOrderByIdAsc(Long requesterId);

	@Query("SELECT r FROM ParticipationRequest r JOIN FETCH r.event WHERE r.requester.id = :userId ORDER BY r.id ASC")
	List<ParticipationRequest> findAllByRequesterIdWithEvent(@Param("userId") Long userId);

	@Query("SELECT r FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.id IN :ids")
	List<ParticipationRequest> findAllByEventIdAndIdIn(@Param("eventId") Long eventId, @Param("ids") List<Long> ids);

	List<ParticipationRequest> findAllByEventIdAndStatusOrderByIdAsc(Long eventId, RequestStatus status);
}
