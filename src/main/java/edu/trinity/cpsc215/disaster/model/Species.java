package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Species {
    HUMAN("human"),
    DOG("dog"),
    CAT("cat"),
    YEAST("yeast"),
    MUSHROOM("mushroom"),
    UNKNOWN("unknown");

    private final String value;

    Species(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static Species fromValue(String value) {
        for (Species s : values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        return UNKNOWN;
    }

    public boolean isTargetSpecies() {
        return this == HUMAN || this == DOG || this == CAT;
    }
}
