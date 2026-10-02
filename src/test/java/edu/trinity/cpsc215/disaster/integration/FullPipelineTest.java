package edu.trinity.cpsc215.disaster.integration;

import edu.trinity.cpsc215.disaster.engine.MatchEngine;
import edu.trinity.cpsc215.disaster.io.EventFileParser;
import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that runs the full pipeline against the small dataset.
 * Verifies correct matches, noise filtering, disaster zone, and statistics.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullPipelineTest {

    private static MatchEngine engine;

    @BeforeAll
    static void setUpOnce() throws Exception {
        EventFileParser parser = new EventFileParser();
        Path dataDir = Path.of("src/main/resources/data");

        List<FoundPerson> found = parser.parseFoundPersons(dataDir.resolve("found-persons-small.json"));
        List<MissingReport> missing = parser.parseMissingReports(dataDir.resolve("missing-reports-small.json"));
        List<Event> events = parser.parseEvents(dataDir.resolve("events-small.json"));

        engine = new MatchEngine();
        engine.loadData(found, missing);
        engine.processEvents(events);
    }

    @Test
    @Order(1)
    void testAllEventsProcessed() {
        assertEquals(125, engine.getTotalEventsProcessed());
    }

    @Test
    @Order(2)
    void testNoiseFiltered() {
        assertTrue(engine.getFilteredNoiseCount() > 0,
                "Should filter out yeast/mushroom samples");
        assertEquals(15, engine.getFilteredNoiseCount(),
                "Should filter exactly 15 noise samples");
    }

    @Test
    @Order(3)
    void testMatchesFound() {
        assertTrue(engine.getMatchCount() > 0, "Should find at least some matches");
        // We generated 25 guaranteed matches; some may not match due to
        // degradation and event ordering, but most should
        assertTrue(engine.getMatchCount() >= 15,
                "Should find at least 15 of 25 guaranteed matches, found " + engine.getMatchCount());
    }

    @Test
    @Order(4)
    void testNoPersonMatchedTwice() {
        List<MatchResult> matches = engine.getMatches();
        long uniqueFoundIds = matches.stream()
                .map(MatchResult::getFoundPersonId).distinct().count();
        long uniqueMissingIds = matches.stream()
                .map(MatchResult::getMissingReportId).distinct().count();

        assertEquals(matches.size(), uniqueFoundIds,
                "Each found person should match at most once");
        assertEquals(matches.size(), uniqueMissingIds,
                "Each missing report should match at most once");
    }

    @Test
    @Order(5)
    void testMatchConfidenceAboveThreshold() {
        for (MatchResult match : engine.getMatches()) {
            assertTrue(match.getConfidence() >= 0.40,
                    "Match confidence should be >= 40%, got " + match.getConfidence()
                            + " for " + match.getFoundPersonId() + " <-> " + match.getMissingReportId());
        }
    }

    @Test
    @Order(6)
    void testDisasterZoneCentroidNearIstanbul() {
        DisasterZone zone = engine.getDisasterZone();
        assertNotNull(zone, "Disaster zone should be calculated");

        GpsCoordinate centroid = zone.getCentroid();
        // Istanbul center: ~41.0082, 28.9784
        assertEquals(41.0, centroid.getLatitude(), 0.2,
                "Centroid latitude should be near Istanbul (41.0)");
        assertEquals(29.0, centroid.getLongitude(), 0.2,
                "Centroid longitude should be near Istanbul (29.0)");
    }

    @Test
    @Order(7)
    void testDisasterZoneAreaReasonable() {
        DisasterZone zone = engine.getDisasterZone();
        assertNotNull(zone);

        // With Gaussian distribution radius ~0.15 degrees, area should be
        // roughly in the hundreds of km^2 range
        assertTrue(zone.getAreaKmSquared() > 50,
                "Area should be > 50 km^2, got " + zone.getAreaKmSquared());
        assertTrue(zone.getAreaKmSquared() < 5000,
                "Area should be < 5000 km^2, got " + zone.getAreaKmSquared());
    }

    @Test
    @Order(8)
    void testFoundAliveAndDeceasedCounts() {
        assertTrue(engine.getFoundAliveCount() > 0, "Should have alive persons");
        assertTrue(engine.getFoundDeceasedCount() > 0, "Should have deceased persons");
        assertEquals(70, engine.getFoundAliveCount() + engine.getFoundDeceasedCount(),
                "Alive + deceased should equal total non-noise found persons");
    }

    @Test
    @Order(9)
    void testUnmatchedCounts() {
        int totalMatches = engine.getMatchCount();
        int unmatchedMissing = engine.getUnmatchedMissingIds().size();
        int unmatchedFound = engine.getUnmatchedFoundIds().size();

        assertEquals(40, totalMatches + unmatchedMissing,
                "Matches + unmatched missing should equal total missing reports (40)");
        assertTrue(unmatchedFound >= 0, "Unmatched found count should be non-negative");
    }

    @Test
    @Order(10)
    void testMatchEvidenceChains() {
        for (MatchResult match : engine.getMatches()) {
            assertNotNull(match.getEvidenceChain(), "Each match should have evidence chain");
            assertFalse(match.getEvidenceChain().isEmpty(),
                    "Evidence chain should not be empty for " + match);
        }
    }
}
