package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.MissingReport;

/**
 * A FIFO queue for processing missing person reports in order of submission.
 * Implemented as a singly linked list with head and tail pointers.
 */
public class ReportQueue {

    private static class Node {
        MissingReport report;
        Node next;

        Node(MissingReport report) {
            this.report = report;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public ReportQueue() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Add a missing report to the back of the queue.
     * Handle the case where the queue is empty (head and tail both null).
     */
    public void enqueue(MissingReport report) {
        Node newNode = new Node(report);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Remove and return the report at the front of the queue.
     * Handle the case where removing makes the queue empty (reset tail).
     * Throw IllegalStateException if empty.
     */
    public MissingReport dequeue() {
        if (head == null) throw new IllegalStateException("Queue is empty");
        MissingReport report = head.report;
        head = head.next;

        if (head == null) {
            tail = null;
        }
        size--;
        return report;
    }

    /**
     * Return the report at the front without removing.
     * Throw IllegalStateException if empty.
     */
    public MissingReport peek() {
        if (head == null) throw new IllegalStateException("Queue is empty");
        return head.report;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}
