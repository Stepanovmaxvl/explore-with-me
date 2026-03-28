package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.dto.ApiError;
import ru.practicum.ewm.util.EwmDateTime;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException e) {
		return build(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
		return build(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException e) {
		return build(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", e.getMessage());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiError> handleBusiness(BusinessRuleException e) {
		return build(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", e.getMessage(),
				e.getApiStatus());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e) {
		return build(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMostSpecificCause().getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(err -> "Field: " + err.getField() + ". Error: " + err.getDefaultMessage()
						+ ". Value: " + err.getRejectedValue())
				.orElse("Validation error");
		return build(HttpStatus.BAD_REQUEST, "Incorrectly made request.", msg);
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
			ConstraintViolationException.class})
	public ResponseEntity<ApiError> handleBadRequest(Exception e) {
		return build(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage());
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String reason, String message) {
		return build(status, reason, message, status.name());
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String reason, String message, String apiStatus) {
		ApiError body = ApiError.builder()
				.status(apiStatus)
				.reason(reason)
				.message(message)
				.timestamp(EwmDateTime.format(LocalDateTime.now()))
				.build();
		return ResponseEntity.status(status).body(body);
	}
}
