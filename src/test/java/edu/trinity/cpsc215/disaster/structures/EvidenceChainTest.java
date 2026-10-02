package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.EvidenceNode;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvidenceChainTest {

    private EvidenceChain chain;

    @BeforeEach
    void setUp() {
        chain = new EvidenceChain();
    }

    @Test
    @Order(1)
    void testEmptyChain() {
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
    }

    @Test
    @Order(2)
    void testAppendAndSize() {
        chain.append(new EvidenceNode("D3S1358", true, 1.0, "match"));
        chain.append(new EvidenceNode("TH01", false, 0.5, "no match"));

        assertEquals(2, chain.size());
        assertFalse(chain.isEmpty());
    }

    @Test
    @Order(3)
    void testGetFirst() {
        chain.append(new EvidenceNode("D3S1358", true, 1.0, "first"));
        chain.append(new EvidenceNode("TH01", false, 0.5, "second"));

        assertEquals("D3S1358", chain.getFirst().getLocusName());
    }

    @Test
    @Order(4)
    void testGetLast() {
        chain.append(new EvidenceNode("D3S1358", true, 1.0, "first"));
        chain.append(new EvidenceNode("TH01", false, 0.5, "second"));

        assertEquals("TH01", chain.getLast().getLocusName());
    }

    @Test
    @Order(5)
    void testToList() {
        chain.append(new EvidenceNode("D3S1358", true, 1.0, "a"));
        chain.append(new EvidenceNode("TH01", false, 0.5, "b"));
        chain.append(new EvidenceNode("FGA", true, 0.67, "c"));

        List<EvidenceNode> list = chain.toList();
        assertEquals(3, list.size());
        assertEquals("D3S1358", list.get(0).getLocusName());
        assertEquals("TH01", list.get(1).getLocusName());
        assertEquals("FGA", list.get(2).getLocusName());
    }

    @Test
    @Order(6)
    void testIterator() {
        chain.append(new EvidenceNode("D3S1358", true, 1.0, "a"));
        chain.append(new EvidenceNode("TH01", true, 1.0, "b"));

        int count = 0;
        for (EvidenceNode node : chain) {
            assertTrue(node.isMatched());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    @Order(7)
    void testGetFirstOnEmptyThrows() {
        assertThrows(IllegalStateException.class, () -> chain.getFirst());
    }
}
