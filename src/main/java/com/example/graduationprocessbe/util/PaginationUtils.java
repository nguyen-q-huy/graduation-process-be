package com.example.graduationprocessbe.util;

import com.example.graduationprocessbe.dto.response.PageResponse;

import java.util.List;

public final class PaginationUtils {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtils() {
    }

    public static <T> PageResponse<T> page(List<T> source, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : size;
        int total = source == null ? 0 : source.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<T> content = source == null ? List.of() : source.subList(from, to);

        return new PageResponse<>(
                content,
                total,
                safePage,
                safeSize,
                totalPages,
                safePage <= 1,
                totalPages == 0 || safePage >= totalPages
        );
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
