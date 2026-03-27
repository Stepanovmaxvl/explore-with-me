package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.User;

import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {

	Page<User> findAllByOrderByIdAsc(Pageable pageable);

	Page<User> findAllByIdInOrderByIdAsc(Collection<Long> ids, Pageable pageable);
}
