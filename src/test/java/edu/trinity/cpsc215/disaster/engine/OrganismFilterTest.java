package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DnaProfile;
import edu.trinity.cpsc215.disaster.model.DnaProfile.AllelePair;
import edu.trinity.cpsc215.disaster.model.Species;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrganismFilterTest {

    private OrganismFilter filter;

    @BeforeEach
    void setUp() {
        filter = new OrganismFilter();
    }

    @Test
    @Order(1)
    void testIdentifyHuman() {
        DnaProfile profile = new DnaProfile(Map.of(
                "D3S1358", new AllelePair(15.0, 17.0),
                "TH01", new AllelePair(6.0, 9.0),
                "D21S11", new AllelePair(29.0, 30.0),
                "FGA", new AllelePair(22.0, 24.0)
        ));
        assertEquals(Species.HUMAN, filter.identifySpecies(profile));
        assertTrue(filter.isTargetSpecies(profile));
    }

    @Test
    @Order(2)
    void testIdentifyDog() {
        DnaProfile profile = new DnaProfile(Map.of(
                "FH2054", new AllelePair(140.0, 148.0),
                "PEZ01", new AllelePair(112.0, 120.0),
                "PEZ03", new AllelePair(134.0, 142.0),
                "AHT121", new AllelePair(84.0, 92.0)
        ));
        assertEquals(Species.DOG, filter.identifySpecies(profile));
        assertTrue(filter.isTargetSpecies(profile));
    }

    @Test
    @Order(3)
    void testIdentifyCat() {
        DnaProfile profile = new DnaProfile(Map.of(
                "FCA441", new AllelePair(133.0, 141.0),
                "FCA723", new AllelePair(214.0, 222.0),
                "FCA740", new AllelePair(176.0, 184.0)
        ));
        assertEquals(Species.CAT, filter.identifySpecies(profile));
        assertTrue(filter.isTargetSpecies(profile));
    }

    @Test
    @Order(4)
    void testIdentifyYeast() {
        DnaProfile profile = new DnaProfile(Map.of(
                "YOR100C", new AllelePair(3.0, 7.0),
                "YDR210W", new AllelePair(4.0, 8.0),
                "YGL115W", new AllelePair(2.0, 6.0)
        ));
        assertEquals(Species.YEAST, filter.identifySpecies(profile));
        assertFalse(filter.isTargetSpecies(profile));
    }

    @Test
    @Order(5)
    void testIdentifyMushroom() {
        DnaProfile profile = new DnaProfile(Map.of(
                "AG_SSR01", new AllelePair(10.0, 14.0),
                "AG_SSR02", new AllelePair(8.0, 12.0),
                "AB_STR01", new AllelePair(11.0, 15.0)
        ));
        assertEquals(Species.MUSHROOM, filter.identifySpecies(profile));
        assertFalse(filter.isTargetSpecies(profile));
    }

    @Test
    @Order(6)
    void testUnknownLoci() {
        DnaProfile profile = new DnaProfile(Map.of(
                "FAKE_LOCUS_1", new AllelePair(1.0, 2.0)
        ));
        assertEquals(Species.UNKNOWN, filter.identifySpecies(profile));
        assertFalse(filter.isTargetSpecies(profile));
    }
}
