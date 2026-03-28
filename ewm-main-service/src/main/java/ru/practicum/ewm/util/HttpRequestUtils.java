package ru.practicum.ewm.util;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpRequestUtils {

	private HttpRequestUtils() {
	}

	public static String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
