package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.FoundPerson;

/**
 * A stack used during DNA matching to track candidate exploration.
 * Supports backtracking: when a candidate's confidence drops below
 * threshold during multi-locus comparison, pop and try the next candidate.
 * Implemented as a singly linked list.
 */
public class MatchStack {

    public record CandidateState(FoundPerson person, int lociChecked,
                                  int lociMatched, double currentConfidence) {
    }

    private static class Node {
        CandidateState state;
        Node next;

        Node(CandidateState state, Node next) {
            this.state = state;
            this.next = next;
        }
    }

    private Node top;
    private int size;

    public MatchStack() {
        top = null;
        size = 0;
    }

    /**
     * Push a candidate state onto the stack.
     * The new node's next should point to the current top.
     */
    public void push(CandidateState state) {
        top = new Node(state, top);
        size++;
    }

    /**
     * Pop the top candidate state off the stack.
     * Throw IllegalStateException if empty.
     */
    public CandidateState pop() {
        if (top == null) throw new IllegalStateException("Stack is empty");
        CandidateState state = top.state;
        top = top.next;
        size--;
        return state;
    }

    /**
     * Peek at the top candidate state without removing.
     * Throw IllegalStateException if empty.
     */
    public CandidateState peek() {
        if (top == null) throw new IllegalStateException("Stack is empty");
        return top.state;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}
