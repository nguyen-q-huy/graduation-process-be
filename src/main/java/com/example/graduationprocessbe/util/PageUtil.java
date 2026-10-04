package com.example.graduationprocessbe.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageUtil {

    private static final int MAX_SIZE = 100;

    private PageUtil() {
    }

    /**
     * Tạo Pageable an toàn từ tham số client: page bắt đầu từ 0, size tối đa 100,
     * sortBy chỉ nhận field trong whitelist (ngoài whitelist thì dùng defaultSort).
     */
    public static Pageable of(int page, int size, String sortBy, String direction,
                              Set<String> allowedSortFields, String defaultSort) {
        int safePage = Math.max(page, 1) - 1; // page từ client bắt đầu từ 1
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        String field = sortBy != null && allowedSortFields.contains(sortBy) ? sortBy : defaultSort;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        // Sort phụ theo id để thứ tự ổn định giữa các trang khi cột sort có giá trị trùng/null.
        Sort sort = Sort.by(dir, field);
        if (!"id".equals(field)) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    /** Chuỗi tìm kiếm dạng LIKE không phân biệt hoa thường; escape ký tự đặc biệt của LIKE. */
    public static String likePattern(String keyword) {
        String escaped = keyword.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
