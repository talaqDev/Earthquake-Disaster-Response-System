package edu.trinity.cpsc215.disaster.structures;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MatchStackTest {

    private MatchStack stack;

    @BeforeEach
    void setUp() {
        stack = new MatchStack();
    }

    @Test
    @Order(1)
    void testEmptyStack() {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    @Order(2)
    void testPushAndPop() {
        var state = new MatchStack.CandidateState(null, 3, 2, 0.67);
        stack.push(state);

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());

        var popped = stack.pop();
        assertEquals(3, popped.lociChecked());
        assertEquals(0.67, popped.currentConfidence(), 0.001);
        assertTrue(stack.isEmpty());
    }

    @Test
    @Order(3)
    void testLIFOOrder() {
        stack.push(new MatchStack.CandidateState(null, 1, 1, 0.5));
        stack.push(new MatchStack.CandidateState(null, 2, 2, 0.8));
        stack.push(new MatchStack.CandidateState(null, 3, 3, 0.9));

        assertEquals(3, stack.pop().lociChecked());
        assertEquals(2, stack.pop().lociChecked());
        assertEquals(1, stack.pop().lociChecked());
    }

    @Test
    @Order(4)
    void testPopOnEmptyThrows() {
        assertThrows(IllegalStateException.class, () -> stack.pop());
    }

    @Test
    @Order(5)
    void testPeekDoesNotRemove() {
        stack.push(new MatchStack.CandidateState(null, 5, 4, 0.8));
        stack.peek();
        assertEquals(1, stack.size());
    }
}
