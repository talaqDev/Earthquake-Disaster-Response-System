package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.FoundPerson;
import java.util.ArrayList;
import java.util.List;

/**
 * A max-heap priority queue for triaging found persons.
 * Higher priority persons (alive, children, elderly) are dequeued first.
 * Implemented as an array-based binary heap.
 *
 * Priority is determined by FoundPerson.getTriagePriority():
 *   - Alive: +100
 *   - Child (age < 18): +50
 *   - Elderly (age > 65): +30
 *
 * Heap property: parent's priority >= both children's priorities
 * Parent index: (i - 1) / 2
 * Left child:   2 * i + 1
 * Right child:  2 * i + 2
 */
public class TriageQueue {

    private final List<FoundPerson> heap;

    public TriageQueue() {
        this.heap = new ArrayList<>();
    }

    /**
     * Insert a found person into the priority queue.
     * Add to end of array, then sift up to restore heap property.
     */
    public void enqueue(FoundPerson person) {
        heap.add(person);
        siftUp(heap.size() - 1);
    }

    /**
     * Remove and return the highest-priority found person.
     * Replace root with last element, then sift down to restore heap property.
     * Throw IllegalStateException if empty.
     */
    public FoundPerson dequeue() {
        if (heap.isEmpty()) throw new IllegalStateException("Queue is empty");

        FoundPerson highest = heap.get(0);
        int lastIndex = heap.size() - 1;

        if (lastIndex == 0) {
            heap.remove(0);
            return highest;
        }

        heap.set(0, heap.get(lastIndex));
        heap.remove(lastIndex);
        siftDown(0);

        return highest;
    }

    /**
     * Return the highest-priority found person without removing.
     * Throw IllegalStateException if empty.
     */
    public FoundPerson peek() {
        if (heap.isEmpty()) throw new IllegalStateException("Queue is empty");
        return heap.get(0);
    }

    /**
     * Restore heap property by moving element at index up toward root.
     * Compare with parent; swap if current has higher priority.
     */
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;

            if (priority(index) > priority(parentIndex)) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    /**
     * Restore heap property by moving element at index down toward leaves.
     * Compare with both children; swap with the higher-priority child.
     */
    private void siftDown(int index) {
        int size = heap.size();

        while (true) {
            int left  = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && priority(left) > priority(largest)) {
                largest = left;
            }

            if (right < size && priority(right) > priority(largest)) {
                largest = right;
            }

            if (largest == index) break;

            swap(index, largest);
            index = largest;
        }
    }

    private int priority(int index) {
        return heap.get(index).getTriagePriority();
    }

    private void swap(int i, int j) {
        FoundPerson temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public int size() { return heap.size(); }
    public boolean isEmpty() { return heap.isEmpty(); }
}
