package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Hit;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
}
