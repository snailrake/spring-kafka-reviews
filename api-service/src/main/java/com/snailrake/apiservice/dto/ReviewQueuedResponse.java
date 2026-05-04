package com.snailrake.apiservice.dto;

public record ReviewQueuedResponse(
        String status,
        String eventId
) {
}