package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CandidateTreeTest {

    private CandidateTree tree;

    private FoundPerson makePerson(String id, double primaryAllele) {
        Map<String, DnaProfile.AllelePair> loci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(primaryAllele, 17.0)
        );
        return new FoundPerson(id, Species.HUMAN, Status.ALIVE, 30,
                new GpsCoordinate(41.0, 29.0), new DnaProfile(loci));
    }

    @BeforeEach
    void setUp() {
        tree = new CandidateTree();
    }

    @Test
    @Order(1)
    void testEmptyTree() {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
    }

    @Test
    @Order(2)
    void testInsertAndSize() {
        tree.insert(makePerson("FP-001", 15.0));
        tree.insert(makePerson("FP-002", 12.0));
        tree.insert(makePerson("FP-003", 18.0));

        assertFalse(tree.isEmpty());
        assertEquals(3, tree.size());
    }

    @Test
    @Order(3)
    void testFindExact() {
        tree.insert(makePerson("FP-001", 15.0));
        tree.insert(makePerson("FP-002", 12.0));
        tree.insert(makePerson("FP-003", 18.0));

        List<FoundPerson> results = tree.findExact(15.0);
        assertEquals(1, results.size());
        assertEquals("FP-001", results.get(0).getId());
    }

    @Test
    @Order(4)
    void testFindExactMiss() {
        tree.insert(makePerson("FP-001", 15.0));
        List<FoundPerson> results = tree.findExact(99.0);
        assertTrue(results.isEmpty());
    }

    @Test
    @Order(5)
    void testFindExactDuplicateKeys() {
        tree.insert(makePerson("FP-001", 15.0));
        tree.insert(makePerson("FP-002", 15.0));

        List<FoundPerson> results = tree.findExact(15.0);
        assertEquals(2, results.size());
    }

    @Test
    @Order(6)
    void testRangeQuery() {
        tree.insert(makePerson("FP-001", 10.0));
        tree.insert(makePerson("FP-002", 15.0));
        tree.insert(makePerson("FP-003", 20.0));
        tree.insert(makePerson("FP-004", 25.0));
        tree.insert(makePerson("FP-005", 5.0));

        List<FoundPerson> results = tree.findInRange(12.0, 22.0);
        assertEquals(2, results.size());
    }

    @Test
    @Order(7)
    void testInOrderTraversal() {
        tree.insert(makePerson("FP-003", 20.0));
        tree.insert(makePerson("FP-001", 10.0));
        tree.insert(makePerson("FP-002", 15.0));

        List<FoundPerson> ordered = tree.inOrder();
        assertEquals(3, ordered.size());
        assertEquals("FP-001", ordered.get(0).getId());
        assertEquals("FP-002", ordered.get(1).getId());
        assertEquals("FP-003", ordered.get(2).getId());
    }

    @Test
    @Order(8)
    void testHeight() {
        tree.insert(makePerson("FP-001", 10.0));
        assertEquals(0, tree.height());

        tree.insert(makePerson("FP-002", 5.0));
        tree.insert(makePerson("FP-003", 15.0));
        assertEquals(1, tree.height());
    }
}
