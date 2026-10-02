package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.FoundPerson;
import edu.trinity.cpsc215.disaster.model.GpsCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A graph where found persons are vertices, and edges connect persons
 * found within a proximity threshold of each other.
 * Used to cluster nearby found persons who may be related (same family).
 * BFS traversal finds connected components (clusters).
 *
 * Implementation: adjacency list using HashMap<String, List<String>>
 */
public class LocationGraph {

    private final Map<String, FoundPerson> vertices;
    private final Map<String, List<String>> adjacencyList;

    public LocationGraph() {
        this.vertices = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Add a found person as a vertex in the graph.
     * Initialize an empty adjacency list for this vertex.
     */
    public void addVertex(FoundPerson person) {
        String id = person.getId();

        if (!vertices.containsKey(id)) {
            vertices.put(id, person);
            adjacencyList.put(id, new ArrayList<>());
        }
    }

    /**
     * Build edges between all persons within the given proximity threshold (km).
     * For each pair of vertices, compute GPS distance and add an edge
     * (in both directions) if within threshold.
     * Call this after adding all vertices.
     *
     * Hint: use GpsCoordinate.distanceKm() to compute distance.
     */
    public void buildEdges(double proximityThresholdKm) {
        List<String> ids = new ArrayList<>(vertices.keySet());
        int n = ids.size();

        for (int i = 0; i < n; i++) {
            String idA = ids.get(i);
            FoundPerson pA = vertices.get(idA);

            for (int j = i + 1; j < n; j++) {
                String idB = ids.get(j);
                FoundPerson pB = vertices.get(idB);
                double dist = pA.getLocation().distanceKm(pB.getLocation());

                if (dist <= proximityThresholdKm) {
                    adjacencyList.get(idA).add(idB);
                    adjacencyList.get(idB).add(idA);
                }
            }
        }
    }

    /**
     * Find all connected components using BFS.
     * Each component is a cluster of nearby found persons.
     * Iterate all vertices; for each unvisited vertex, run BFS
     * to collect its entire connected component.
     */
    public List<List<FoundPerson>> findClusters() {
        List<List<FoundPerson>> clusters = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (String id : vertices.keySet()) {
            if (!visited.contains(id)) {
                clusters.add(bfs(id, visited));
            }
        }
        return clusters;
    }

    /**
     * BFS from a starting vertex, returning all reachable persons.
     * Mark visited vertices in the provided Set to avoid revisiting.
     *
     * Algorithm:
     *   1. Create a Queue, add startId, mark as visited
     *   2. While queue is not empty:
     *      a. Poll the next vertex
     *      b. Add its FoundPerson to the result list
     *      c. For each unvisited neighbor, mark visited and enqueue
     *   3. Return the result list
     */
    public List<FoundPerson> bfs(String startId, Set<String> visited) {
        List<FoundPerson> component = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();

        visited.add(startId);
        queue.add(startId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            component.add(vertices.get(currentId));

            List<String> neighbors = adjacencyList.get(currentId);
            if (neighbors != null) {
                for (String neighborId : neighbors) {
                    if (!visited.contains(neighborId)) {
                        visited.add(neighborId);
                        queue.add(neighborId);
                    }
                }
            }
        }
        return component;
    }

    /**
     * Find all persons within a given distance of a location.
     */
    public List<FoundPerson> findNearby(GpsCoordinate location, double radiusKm) {
        List<FoundPerson> nearby = new ArrayList<>();
        for (FoundPerson p : vertices.values()) {
            if (location.distanceKm(p.getLocation()) <= radiusKm) {
                nearby.add(p);
            }
        }
        return nearby;
    }

    public int vertexCount() { return vertices.size(); }

    public int edgeCount() {
        return adjacencyList.values().stream().mapToInt(List::size).sum() / 2;
    }
}
