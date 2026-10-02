package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.EvidenceNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A singly linked list that records the chain of evidence for a DNA match.
 * Each node records which locus was compared, whether it matched,
 * and the running confidence at that step.
 *
 * Must support: append to tail, get first, get last, convert to List, iterate.
 */
public class EvidenceChain implements Iterable<EvidenceNode> {

    private static class ChainNode {
        EvidenceNode evidence;
        ChainNode next;

        ChainNode(EvidenceNode evidence) {
            this.evidence = evidence;
            this.next = null;
        }
    }

    private ChainNode head;
    private ChainNode tail;
    private int size;

    public EvidenceChain() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Append an evidence node to the end of the chain.
     * Handle the case where the chain is empty.
     */
    public void append(EvidenceNode evidence) {
        ChainNode newNode = new ChainNode(evidence);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }

        size++;
    }

    /**
     * Get the first evidence node in the chain.
     * Throw IllegalStateException if empty.
     */
    public EvidenceNode getFirst() {
        if (isEmpty())throw new IllegalStateException("Chain is empty");
        return head.evidence;
    }

    /**
     * Get the last evidence node (most recent comparison).
     * Throw IllegalStateException if empty.
     */
    public EvidenceNode getLast() {
        if (tail == null) throw new IllegalStateException("Chain is empty");
        return tail.evidence;
    }

    /**
     * Convert the chain to a List by traversing from head to tail.
     */
    public List<EvidenceNode> toList() {
        List<EvidenceNode> list = new ArrayList<>(size);
        ChainNode current = head;

        while (current != null) {
            list.add(current.evidence);
            current = current.next;
        }
        return list;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    /**
     * Return an iterator that traverses the chain from head to tail.
     */
    @Override
    public Iterator<EvidenceNode> iterator() {
        return new Iterator<EvidenceNode>() {
            private ChainNode current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public EvidenceNode next() {
                if (!hasNext()) throw new NoSuchElementException();
                EvidenceNode evidence = current.evidence;
                current = current.next;
                return evidence;
            }
        };
    }
}
