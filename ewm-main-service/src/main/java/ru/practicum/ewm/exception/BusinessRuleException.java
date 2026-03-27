package ru.practicum.ewm.exception;

import lombok.Getter;

@Getter
public class BusinessRuleException extends RuntimeException {

	private final String apiStatus;

	public BusinessRuleException(String apiStatus, String message) {
		super(message);
		this.apiStatus = apiStatus;
	}
}
