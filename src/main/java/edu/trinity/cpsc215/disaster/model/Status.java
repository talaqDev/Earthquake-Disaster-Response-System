package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    ALIVE("alive"),
    DECEASED("deceased");

    private final String value;

    Status(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status s : values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        return DECEASED;
    }
}
