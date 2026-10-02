package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportQueueTest {

    private ReportQueue queue;

    private MissingReport makeReport(String id) {
        Map<String, DnaProfile.AllelePair> loci = Map.of(
                "D3S1358", new DnaProfile.AllelePair(15.0, 17.0)
        );
        return new MissingReport(id, "Test Person", Species.HUMAN,
                "self", "Submitter", new DnaProfile(loci));
    }

    @BeforeEach
    void setUp() {
        queue = new ReportQueue();
    }

    @Test
    @Order(1)
    void testEmptyQueue() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @Order(2)
    void testEnqueueAndDequeue() {
        queue.enqueue(makeReport("MR-001"));
        queue.enqueue(makeReport("MR-002"));

        assertEquals("MR-001", queue.dequeue().getId());
        assertEquals("MR-002", queue.dequeue().getId());
    }

    @Test
    @Order(3)
    void testFIFOOrder() {
        queue.enqueue(makeReport("first"));
        queue.enqueue(makeReport("second"));
        queue.enqueue(makeReport("third"));

        assertEquals("first", queue.dequeue().getId());
        assertEquals("second", queue.dequeue().getId());
        assertEquals("third", queue.dequeue().getId());
    }

    @Test
    @Order(4)
    void testDequeueOnEmptyThrows() {
        assertThrows(IllegalStateException.class, () -> queue.dequeue());
    }

    @Test
    @Order(5)
    void testPeekDoesNotRemove() {
        queue.enqueue(makeReport("MR-001"));
        assertEquals("MR-001", queue.peek().getId());
        assertEquals(1, queue.size());
    }

    @Test
    @Order(6)
    void testSizeAfterOperations() {
        queue.enqueue(makeReport("MR-001"));
        queue.enqueue(makeReport("MR-002"));
        assertEquals(2, queue.size());

        queue.dequeue();
        assertEquals(1, queue.size());

        queue.dequeue();
        assertTrue(queue.isEmpty());
    }
}
