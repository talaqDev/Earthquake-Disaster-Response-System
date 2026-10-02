package edu.trinity.cpsc215.disaster.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.trinity.cpsc215.disaster.engine.MatchEngine;
import edu.trinity.cpsc215.disaster.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Writes engine results to JSON (for the map visualization and autograder)
 * and to the console (human-readable report).
 */
public class ResultWriter {

    private final ObjectMapper mapper;

    public ResultWriter() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Write results.json for the map visualization and autograder.
     */
    public void writeResultsJson(MatchEngine engine, Path outputPath) throws IOException {
        Map<String, Object> results = buildResultsMap(engine);
        mapper.writeValue(outputPath.toFile(), results);
    }

    /**
     * Print a human-readable report to stdout.
     */
    public void printConsoleReport(MatchEngine engine) {
        System.out.println("=== EARTHQUAKE DISASTER RESPONSE REPORT ===");
        System.out.println();

        // Disaster Zone
        DisasterZone zone = engine.getDisasterZone();
        if (zone != null) {
            System.out.println("DISASTER ZONE:");
            System.out.printf("  Centroid:     %s%n", zone.getCentroid());
            System.out.printf("  Bounding Box: %s to %s%n",
                    zone.getBoundingBoxMin(), zone.getBoundingBoxMax());
            System.out.printf("  Area:         %.2f km^2%n", zone.getAreaKmSquared());
        }
        System.out.println();

        // Statistics
        System.out.println("STATISTICS:");
        System.out.printf("  Events processed:    %d%n", engine.getTotalEventsProcessed());
        System.out.printf("  Found alive:         %d%n", engine.getFoundAliveCount());
        System.out.printf("  Found deceased:      %d%n", engine.getFoundDeceasedCount());
        System.out.printf("  Noise filtered:      %d%n", engine.getFilteredNoiseCount());
        System.out.printf("  Matches found:       %d%n", engine.getMatchCount());
        System.out.printf("  Unmatched missing:   %d%n", engine.getUnmatchedMissingIds().size());
        System.out.printf("  Unmatched found:     %d%n", engine.getUnmatchedFoundIds().size());
        System.out.printf("  Processing time:     %,d ns (%.3f ms)%n",
                engine.getProcessingTimeNanos(),
                engine.getProcessingTimeNanos() / 1_000_000.0);
        System.out.println();

        // Matches (first 5, full list in results.json)
        System.out.println("MATCHES:");
        List<MatchResult> allMatches = engine.getMatches();
        int shown = Math.min(5, allMatches.size());
        for (int i = 0; i < shown; i++) {
            MatchResult match = allMatches.get(i);
            MissingReport report = engine.getMissingReportsById().get(match.getMissingReportId());
            FoundPerson found = engine.getFoundPersonsById().get(match.getFoundPersonId());
            String name = report != null ? report.getName() : "Unknown";
            String status = found != null ? found.getStatus().getValue() : "unknown";
            System.out.printf("  %s <-> %s  (%s, confidence: %.1f%%, type: %s, status: %s)%n",
                    match.getFoundPersonId(), match.getMissingReportId(),
                    name, match.getConfidence() * 100, match.getMatchType(), status);
        }
        if (allMatches.size() > 5) {
            System.out.printf("  ... and %d more (see results.json)%n", allMatches.size() - 5);
        }
        System.out.println();

        System.out.println("=== END REPORT ===");
    }

    private Map<String, Object> buildResultsMap(MatchEngine engine) {
        Map<String, Object> results = new LinkedHashMap<>();

        // Disaster zone
        DisasterZone zone = engine.getDisasterZone();
        if (zone != null) {
            Map<String, Object> zoneMap = new LinkedHashMap<>();
            zoneMap.put("centroid", coordMap(zone.getCentroid()));
            zoneMap.put("boundingBoxMin", coordMap(zone.getBoundingBoxMin()));
            zoneMap.put("boundingBoxMax", coordMap(zone.getBoundingBoxMax()));
            zoneMap.put("areaKmSquared", Math.round(zone.getAreaKmSquared() * 100.0) / 100.0);
            results.put("disasterZone", zoneMap);
        }

        // Statistics
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("eventsProcessed", engine.getTotalEventsProcessed());
        stats.put("foundAlive", engine.getFoundAliveCount());
        stats.put("foundDeceased", engine.getFoundDeceasedCount());
        stats.put("noiseFiltered", engine.getFilteredNoiseCount());
        stats.put("matchesFound", engine.getMatchCount());
        stats.put("unmatchedMissing", engine.getUnmatchedMissingIds().size());
        stats.put("unmatchedFound", engine.getUnmatchedFoundIds().size());
        stats.put("processingTimeNanos", engine.getProcessingTimeNanos());
        stats.put("processingTimeMs", Math.round(engine.getProcessingTimeNanos() / 1_000_000.0 * 1000.0) / 1000.0);
        results.put("statistics", stats);

        // Matches with GPS for map rendering
        List<Map<String, Object>> matchList = new ArrayList<>();
        for (MatchResult match : engine.getMatches()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("foundPersonId", match.getFoundPersonId());
            m.put("missingReportId", match.getMissingReportId());
            m.put("confidence", Math.round(match.getConfidence() * 1000.0) / 1000.0);
            m.put("matchType", match.getMatchType());

            FoundPerson fp = engine.getFoundPersonsById().get(match.getFoundPersonId());
            if (fp != null && fp.getLocation() != null) {
                m.put("location", coordMap(fp.getLocation()));
            }
            MissingReport mr = engine.getMissingReportsById().get(match.getMissingReportId());
            if (mr != null) {
                m.put("missingName", mr.getName());
            }
            if (fp != null) {
                m.put("status", fp.getStatus().getValue());
            }

            matchList.add(m);
        }
        results.put("matches", matchList);

        // Unmatched found persons (with GPS for map)
        List<Map<String, Object>> unmatchedFound = new ArrayList<>();
        for (String id : engine.getUnmatchedFoundIds()) {
            FoundPerson fp = engine.getFoundPersonsById().get(id);
            if (fp != null && fp.getLocation() != null) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", id);
                m.put("location", coordMap(fp.getLocation()));
                m.put("status", fp.getStatus().getValue());
                m.put("species", fp.getSpecies().getValue());
                unmatchedFound.add(m);
            }
        }
        results.put("unmatchedFound", unmatchedFound);

        // Unmatched missing IDs
        List<Map<String, Object>> unmatchedMissing = new ArrayList<>();
        for (String id : engine.getUnmatchedMissingIds()) {
            MissingReport mr = engine.getMissingReportsById().get(id);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            if (mr != null) m.put("name", mr.getName());
            unmatchedMissing.add(m);
        }
        results.put("unmatchedMissing", unmatchedMissing);

        return results;
    }

    private Map<String, Double> coordMap(GpsCoordinate coord) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("lat", Math.round(coord.getLatitude() * 10000.0) / 10000.0);
        m.put("lon", Math.round(coord.getLongitude() * 10000.0) / 10000.0);
        return m;
    }
}
