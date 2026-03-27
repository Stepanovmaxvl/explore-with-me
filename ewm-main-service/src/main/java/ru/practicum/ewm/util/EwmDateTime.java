package ru.practicum.ewm.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EwmDateTime {

	public static final DateTimeFormatter API_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private EwmDateTime() {
	}

	public static LocalDateTime parse(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return LocalDateTime.parse(value.trim(), API_DATE_TIME);
	}

	public static String format(LocalDateTime dt) {
		if (dt == null) {
			return null;
		}
		return dt.format(API_DATE_TIME);
	}
}
