package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {

    public enum EventType {
        FOUND, MISSING
    }

    private final EventType type;
    private final String refId;
    private final String timestamp;

    @JsonCreator
    public Event(
            @JsonProperty("type") EventType type,
            @JsonProperty("refId") String refId,
            @JsonProperty("timestamp") String timestamp) {
        this.type = type;
        this.refId = refId;
        this.timestamp = timestamp;
    }

    public EventType getType() { return type; }
    public String getRefId() { return refId; }
    public String getTimestamp() { return timestamp; }
}
