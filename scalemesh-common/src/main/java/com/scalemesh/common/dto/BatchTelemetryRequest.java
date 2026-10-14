package com.scalemesh.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BatchTelemetryRequest {

    @NotEmpty(message = "Events list cannot be empty")
    @Valid
    private List<TelemetryEventDto> events;

    public BatchTelemetryRequest() {
    }

    public BatchTelemetryRequest(List<TelemetryEventDto> events) {
        this.events = events;
    }

    public List<TelemetryEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<TelemetryEventDto> events) {
        this.events = events;
    }
}
