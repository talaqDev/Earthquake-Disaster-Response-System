package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DnaProfile;
import edu.trinity.cpsc215.disaster.model.DnaProfile.AllelePair;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DnaComparatorTest {

    private DnaComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new DnaComparator();
    }

    private DnaProfile makeProfile(Map<String, AllelePair> loci) {
        return new DnaProfile(new LinkedHashMap<>(loci));
    }

    @Test
    @Order(1)
    void testIdenticalProfiles() {
        Map<String, AllelePair> loci = Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0),
                "FGA", new AllelePair(22.0, 24.0)
        );

        DnaComparator.MatchResult result = comparator.compareProfiles(
                makeProfile(loci), makeProfile(loci));

        assertEquals(1.0, result.confidence(), 0.01);
        assertEquals("direct", result.matchType());
    }

    @Test
    @Order(2)
    void testCompletelyDifferentProfiles() {
        DnaProfile p1 = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0),
                "FGA", new AllelePair(22.0, 24.0)
        ));
        DnaProfile p2 = makeProfile(Map.of(
                "D3S1358", new AllelePair(12.0, 14.0),
                "TH01", new AllelePair(5.0, 7.0),
                "FGA", new AllelePair(18.0, 20.0)
        ));

        DnaComparator.MatchResult result = comparator.compareProfiles(p1, p2);
        assertTrue(result.confidence() < DnaComparator.NO_MATCH_THRESHOLD);
        assertEquals("none", result.matchType());
    }

    @Test
    @Order(3)
    void testFamilyMatch() {
        // Parent shares one allele per locus with child
        DnaProfile parent = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0),
                "FGA", new AllelePair(22.0, 24.0),
                "vWA", new AllelePair(16.0, 18.0)
        ));
        DnaProfile child = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 14.0),  // shares 15
                "TH01", new AllelePair(6.0, 7.0),       // shares 6
                "FGA", new AllelePair(22.0, 20.0),       // shares 22
                "vWA", new AllelePair(16.0, 19.0)        // shares 16
        ));

        DnaComparator.MatchResult result = comparator.compareProfiles(parent, child);
        assertTrue(result.confidence() >= DnaComparator.FAMILY_MATCH_THRESHOLD);
        assertTrue(result.confidence() < DnaComparator.DIRECT_MATCH_THRESHOLD);
    }

    @Test
    @Order(4)
    void testNoSharedLoci() {
        DnaProfile p1 = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0)
        ));
        DnaProfile p2 = makeProfile(Map.of(
                "FGA", new AllelePair(22.0, 24.0)
        ));

        DnaComparator.MatchResult result = comparator.compareProfiles(p1, p2);
        assertEquals(0.0, result.confidence());
        assertEquals("no_shared_loci", result.matchType());
    }

    @Test
    @Order(5)
    void testDegradedProfile() {
        DnaProfile full = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0)
        ));
        // Degraded: one allele missing
        DnaProfile degraded = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, null),
                "TH01", new AllelePair(6.0, 9.0)
        ));

        DnaComparator.MatchResult result = comparator.compareProfiles(full, degraded);
        assertTrue(result.confidence() > 0.5);
    }

    @Test
    @Order(6)
    void testEvidenceChainPopulated() {
        DnaProfile p1 = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0)
        ));
        DnaProfile p2 = makeProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0)
        ));

        DnaComparator.MatchResult result = comparator.compareProfiles(p1, p2);
        assertFalse(result.evidence().isEmpty());
        assertEquals(2, result.evidence().size());
    }
}
