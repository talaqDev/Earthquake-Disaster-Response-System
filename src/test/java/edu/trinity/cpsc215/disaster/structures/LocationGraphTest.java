package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocationGraphTest {

    private LocationGraph graph;

    private FoundPerson makePerson(String id, double lat, double lon) {
        Map<String, DnaProfile.AllelePair> loci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(15.0, 17.0)
        );
        return new FoundPerson(id, Species.HUMAN, Status.ALIVE, 30,
                new GpsCoordinate(lat, lon), new DnaProfile(loci));
    }

    @BeforeEach
    void setUp() {
        graph = new LocationGraph();
    }

    @Test
    @Order(1)
    void testEmptyGraph() {
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
    }

    @Test
    @Order(2)
    void testAddVertices() {
        graph.addVertex(makePerson("FP-001", 41.0, 29.0));
        graph.addVertex(makePerson("FP-002", 41.001, 29.001));

        assertEquals(2, graph.vertexCount());
    }

    @Test
    @Order(3)
    void testBuildEdgesNearby() {
        // Two persons ~0.15 km apart
        graph.addVertex(makePerson("FP-001", 41.0000, 29.0000));
        graph.addVertex(makePerson("FP-002", 41.0010, 29.0010));
        graph.buildEdges(1.0);

        assertEquals(1, graph.edgeCount());
    }

    @Test
    @Order(4)
    void testBuildEdgesFarApart() {
        // Two persons ~11 km apart
        graph.addVertex(makePerson("FP-001", 41.0, 29.0));
        graph.addVertex(makePerson("FP-002", 41.1, 29.1));
        graph.buildEdges(1.0);

        assertEquals(0, graph.edgeCount());
    }

    @Test
    @Order(5)
    void testFindClusters() {
        // Cluster 1: two nearby persons
        graph.addVertex(makePerson("FP-001", 41.0000, 29.0000));
        graph.addVertex(makePerson("FP-002", 41.0005, 29.0005));
        // Cluster 2: one isolated person
        graph.addVertex(makePerson("FP-003", 42.0, 30.0));

        graph.buildEdges(1.0);

        List<List<FoundPerson>> clusters = graph.findClusters();
        assertEquals(2, clusters.size());

        // One cluster has 2 members, the other has 1
        List<Integer> sizes = clusters.stream().map(List::size).sorted().toList();
        assertEquals(List.of(1, 2), sizes);
    }

    @Test
    @Order(6)
    void testFindNearby() {
        graph.addVertex(makePerson("FP-001", 41.0, 29.0));
        graph.addVertex(makePerson("FP-002", 41.001, 29.001));
        graph.addVertex(makePerson("FP-003", 42.0, 30.0));

        GpsCoordinate center = new GpsCoordinate(41.0, 29.0);
        List<FoundPerson> nearby = graph.findNearby(center, 1.0);
        assertEquals(2, nearby.size());
    }

    @Test
    @Order(7)
    void testBFS() {
        graph.addVertex(makePerson("A", 41.0000, 29.0000));
        graph.addVertex(makePerson("B", 41.0005, 29.0005));
        graph.addVertex(makePerson("C", 41.0010, 29.0000));
        graph.buildEdges(1.0);

        Set<String> visited = new HashSet<>();
        List<FoundPerson> component = graph.bfs("A", visited);
        assertTrue(component.size() >= 1);
        assertTrue(visited.contains("A"));
    }
}
