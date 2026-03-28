package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EwmDtoMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final EventRepository eventRepository;

	public List<CategoryDto> getAll(int from, int size) {
		return categoryRepository.findAll(PageRequest.of(from / size, size)).getContent().stream()
				.map(EwmDtoMapper::toCategoryDto).collect(Collectors.toList());
	}

	public CategoryDto getById(Long id) {
		Category c = categoryRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
		return EwmDtoMapper.toCategoryDto(c);
	}

	@Transactional
	public CategoryDto create(NewCategoryDto dto) {
		Category c = Category.builder().name(dto.getName()).build();
		categoryRepository.save(c);
		return EwmDtoMapper.toCategoryDto(c);
	}

	@Transactional
	public CategoryDto update(Long catId, CategoryDto dto) {
		Category c = categoryRepository.findById(catId)
				.orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
		c.setName(dto.getName());
		categoryRepository.save(c);
		return EwmDtoMapper.toCategoryDto(c);
	}

	@Transactional
	public void delete(Long catId) {
		if (!categoryRepository.existsById(catId)) {
			throw new NotFoundException("Category with id=" + catId + " was not found");
		}
		if (eventRepository.existsByCategoryId(catId)) {
			throw new ConflictException("The category is not empty");
		}
		categoryRepository.deleteById(catId);
	}
}
