package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

	@Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events e LEFT JOIN FETCH e.category "
			+ "LEFT JOIN FETCH e.initiator WHERE c.id = :id")
	Optional<Compilation> findByIdWithEvents(@Param("id") Long id);

	Page<Compilation> findAllByOrderByPinnedDescIdAsc(Pageable pageable);

	Page<Compilation> findAllByPinnedOrderByIdAsc(boolean pinned, Pageable pageable);
}
