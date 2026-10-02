package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TriageQueueTest {

    private TriageQueue queue;

    private FoundPerson makePerson(String id, Status status, int age) {
        Map<String, DnaProfile.AllelePair> loci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(15.0, 17.0)
        );
        return new FoundPerson(id, Species.HUMAN, status, age,
                new GpsCoordinate(41.0, 29.0), new DnaProfile(loci));
    }

    @BeforeEach
    void setUp() {
        queue = new TriageQueue();
    }

    @Test
    @Order(1)
    void testEmptyQueue() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @Order(2)
    void testEnqueueAndSize() {
        queue.enqueue(makePerson("FP-001", Status.ALIVE, 30));
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    @Order(3)
    void testDequeueOnEmptyThrows() {
        assertThrows(IllegalStateException.class, () -> queue.dequeue());
    }

    @Test
    @Order(4)
    void testAliveBeforeDeceased() {
        FoundPerson deceased = makePerson("FP-001", Status.DECEASED, 30);
        FoundPerson alive = makePerson("FP-002", Status.ALIVE, 30);

        queue.enqueue(deceased);
        queue.enqueue(alive);

        FoundPerson first = queue.dequeue();
        assertEquals("FP-002", first.getId()); // alive has higher priority
    }

    @Test
    @Order(5)
    void testChildrenBeforeAdults() {
        FoundPerson adult = makePerson("FP-001", Status.ALIVE, 30);
        FoundPerson child = makePerson("FP-002", Status.ALIVE, 10);

        queue.enqueue(adult);
        queue.enqueue(child);

        FoundPerson first = queue.dequeue();
        assertEquals("FP-002", first.getId()); // child has higher priority
    }

    @Test
    @Order(6)
    void testElderlyBeforeAdults() {
        FoundPerson adult = makePerson("FP-001", Status.ALIVE, 30);
        FoundPerson elderly = makePerson("FP-002", Status.ALIVE, 70);

        queue.enqueue(adult);
        queue.enqueue(elderly);

        FoundPerson first = queue.dequeue();
        assertEquals("FP-002", first.getId()); // elderly has higher priority
    }

    @Test
    @Order(7)
    void testFullPriorityOrder() {
        queue.enqueue(makePerson("deceased-adult", Status.DECEASED, 30));
        queue.enqueue(makePerson("alive-child", Status.ALIVE, 10));
        queue.enqueue(makePerson("alive-adult", Status.ALIVE, 30));
        queue.enqueue(makePerson("alive-elderly", Status.ALIVE, 70));

        assertEquals("alive-child", queue.dequeue().getId());    // 150
        assertEquals("alive-elderly", queue.dequeue().getId());   // 130
        assertEquals("alive-adult", queue.dequeue().getId());     // 100
        assertEquals("deceased-adult", queue.dequeue().getId());  // 0
    }

    @Test
    @Order(8)
    void testPeekDoesNotRemove() {
        queue.enqueue(makePerson("FP-001", Status.ALIVE, 30));
        queue.peek();
        assertEquals(1, queue.size());
    }
}
