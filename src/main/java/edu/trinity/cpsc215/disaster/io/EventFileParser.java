package edu.trinity.cpsc215.disaster.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.trinity.cpsc215.disaster.model.Event;
import edu.trinity.cpsc215.disaster.model.FoundPerson;
import edu.trinity.cpsc215.disaster.model.MissingReport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parses JSON data files for found persons, missing reports, and events.
 * Supports loading from classpath resources or filesystem paths.
 */
public class EventFileParser {

    private final ObjectMapper mapper;

    public EventFileParser() {
        this.mapper = new ObjectMapper();
    }

    public List<FoundPerson> parseFoundPersons(Path path) throws IOException {
        return mapper.readValue(Files.readString(path),
                new TypeReference<List<FoundPerson>>() {});
    }

    public List<MissingReport> parseMissingReports(Path path) throws IOException {
        return mapper.readValue(Files.readString(path),
                new TypeReference<List<MissingReport>>() {});
    }

    public List<Event> parseEvents(Path path) throws IOException {
        return mapper.readValue(Files.readString(path),
                new TypeReference<List<Event>>() {});
    }

    public List<FoundPerson> parseFoundPersonsResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) throw new IOException("Resource not found: " + resourceName);
            return mapper.readValue(is, new TypeReference<>() {});
        }
    }

    public List<MissingReport> parseMissingReportsResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) throw new IOException("Resource not found: " + resourceName);
            return mapper.readValue(is, new TypeReference<>() {});
        }
    }

    public List<Event> parseEventsResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) throw new IOException("Resource not found: " + resourceName);
            return mapper.readValue(is, new TypeReference<>() {});
        }
    }
}
