package com.udescmaker.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

public class YoutubeUrlValidator implements ConstraintValidator<YoutubeUrl, String> {
    private static final Set<String> YOUTUBE_HOSTS = Set.of(
            "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be", "www.youtu.be");
    private static final Pattern VIDEO_ID = Pattern.compile("[A-Za-z0-9_-]{11}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        try {
            URI uri = URI.create(value.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null
                    || !YOUTUBE_HOSTS.contains(uri.getHost())) return false;
            String id;
            if (uri.getHost().endsWith("youtu.be")) {
                id = singlePathId(uri.getPath(), null);
            } else if (uri.getPath().equals("/watch")) {
                id = queryParameter(uri.getRawQuery(), "v");
            } else if (uri.getPath().startsWith("/shorts/")) {
                id = singlePathId(uri.getPath(), "shorts");
            } else {
                return false;
            }
            return id != null && VIDEO_ID.matcher(id).matches();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String singlePathId(String path, String prefix) {
        if (path == null) return null;
        String[] segments = java.util.Arrays.stream(path.split("/"))
                .filter(segment -> !segment.isBlank()).toArray(String[]::new);
        if (prefix == null) return segments.length == 1 ? segments[0] : null;
        return segments.length == 2 && prefix.equals(segments[0]) ? segments[1] : null;
    }

    private String queryParameter(String query, String key) {
        if (query == null) return null;
        for (String item : query.split("&")) {
            int separator = item.indexOf('=');
            if (separator > 0 && item.substring(0, separator).equals(key)) return item.substring(separator + 1);
        }
        return null;
    }
}
