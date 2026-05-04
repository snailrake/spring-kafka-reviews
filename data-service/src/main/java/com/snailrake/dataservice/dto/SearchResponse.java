package com.snailrake.dataservice.dto;

import java.util.List;

public record SearchResponse(
        int count,
        List<ReviewItem> items
) {
}

