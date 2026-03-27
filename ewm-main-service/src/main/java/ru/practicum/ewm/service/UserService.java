package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public List<UserDto> search(List<Long> ids, int from, int size) {
		if (ids != null && ids.isEmpty()) {
			return Collections.emptyList();
		}
		var pageable = PageRequest.of(from / size, size);
		if (ids == null) {
			return userRepository.findAllByOrderByIdAsc(pageable).getContent().stream()
					.map(EwmDtoMapper::toUserDto).collect(Collectors.toList());
		}
		return userRepository.findAllByIdInOrderByIdAsc(ids, pageable).getContent().stream()
				.map(EwmDtoMapper::toUserDto).collect(Collectors.toList());
	}

	@Transactional
	public UserDto create(NewUserRequest request) {
		User user = User.builder()
				.email(request.getEmail())
				.name(request.getName())
				.build();
		userRepository.save(user);
		return EwmDtoMapper.toUserDto(user);
	}

	@Transactional
	public void delete(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
		userRepository.delete(user);
	}

	public void ensureExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User with id=" + userId + " was not found");
		}
	}
}
