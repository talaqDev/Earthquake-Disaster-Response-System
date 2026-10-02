package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocusIndexTest {

    private LocusIndex index;

    private FoundPerson makePerson(String id, double d3Allele1, double d3Allele2,
                                    double th01Allele1, double th01Allele2) {
        Map<String, DnaProfile.AllelePair> loci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(d3Allele1, d3Allele2),
                "TH01", new DnaProfile.AllelePair(th01Allele1, th01Allele2)
        );
        return new FoundPerson(id, Species.HUMAN, Status.ALIVE, 30,
                new GpsCoordinate(41.0, 29.0), new DnaProfile(loci));
    }

    @BeforeEach
    void setUp() {
        index = new LocusIndex();
    }

    @Test
    @Order(1)
    void testAddPersonAndFindCandidates() {
        FoundPerson person = makePerson("FP-001", 15.0, 17.0, 6.0, 9.0);
        index.addPerson(person);

        List<FoundPerson> candidates = index.findCandidates("D3S1358", 15.0);
        assertEquals(1, candidates.size());
        assertEquals("FP-001", candidates.get(0).getId());
    }

    @Test
    @Order(2)
    void testFindCandidatesSecondAllele() {
        FoundPerson person = makePerson("FP-001", 15.0, 17.0, 6.0, 9.0);
        index.addPerson(person);

        List<FoundPerson> candidates = index.findCandidates("D3S1358", 17.0);
        assertEquals(1, candidates.size());
    }

    @Test
    @Order(3)
    void testFindCandidatesMiss() {
        FoundPerson person = makePerson("FP-001", 15.0, 17.0, 6.0, 9.0);
        index.addPerson(person);

        List<FoundPerson> candidates = index.findCandidates("D3S1358", 99.0);
        assertTrue(candidates.isEmpty());
    }

    @Test
    @Order(4)
    void testMultiplePersonsSameLocus() {
        index.addPerson(makePerson("FP-001", 15.0, 17.0, 6.0, 9.0));
        index.addPerson(makePerson("FP-002", 15.0, 18.0, 7.0, 9.0));
        index.addPerson(makePerson("FP-003", 12.0, 14.0, 8.0, 10.0));

        List<FoundPerson> candidates = index.findCandidates("D3S1358", 15.0);
        assertEquals(2, candidates.size());
    }

    @Test
    @Order(5)
    void testFindCandidatesForLocus() {
        index.addPerson(makePerson("FP-001", 15.0, 17.0, 6.0, 9.0));
        index.addPerson(makePerson("FP-002", 12.0, 15.0, 6.0, 8.0));

        DnaProfile.AllelePair query = new DnaProfile.AllelePair(15.0, 18.0);
        Set<FoundPerson> candidates = index.findCandidatesForLocus("D3S1358", query);
        assertEquals(2, candidates.size()); // Both have allele 15 at D3S1358
    }

    @Test
    @Order(6)
    void testFindCandidatesMultiLocus() {
        index.addPerson(makePerson("FP-001", 15.0, 17.0, 6.0, 9.0));
        index.addPerson(makePerson("FP-002", 15.0, 18.0, 7.0, 10.0));

        // Query matches FP-001 at both loci, FP-002 only at D3S1358
        Map<String, DnaProfile.AllelePair> queryLoci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(15.0, 17.0),
                "TH01", new DnaProfile.AllelePair(6.0, 9.0)
        );
        DnaProfile queryProfile = new DnaProfile(queryLoci);

        Set<FoundPerson> candidates = index.findCandidatesMultiLocus(queryProfile, 2);
        assertEquals(1, candidates.size());
        assertEquals("FP-001", candidates.iterator().next().getId());
    }

    @Test
    @Order(7)
    void testGetIndexedLoci() {
        index.addPerson(makePerson("FP-001", 15.0, 17.0, 6.0, 9.0));
        Set<String> loci = index.getIndexedLoci();
        assertTrue(loci.contains("D3S1358"));
        assertTrue(loci.contains("TH01"));
    }
}
