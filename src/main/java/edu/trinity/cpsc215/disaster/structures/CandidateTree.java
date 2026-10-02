package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.FoundPerson;
import java.util.ArrayList;
import java.util.List;

/**
 * A Binary Search Tree that indexes FoundPerson records by a numeric key
 * derived from their DNA profile's primary locus allele value.
 * Supports insertion, search, range queries, and in-order traversal.
 */
public class CandidateTree {

    private static class Node {
        double key;
        FoundPerson person;
        List<FoundPerson> duplicates;
        Node left, right;

        Node(double key, FoundPerson person) {
            this.key = key;
            this.person = person;
            this.duplicates = new ArrayList<>();
        }
    }

    private Node root;
    private int size;

    public CandidateTree() {
        root = null;
        size = 0;
    }

    /**
     * Insert a found person into the tree, keyed by their primary DNA index value.
     * Use person.getDnaProfile().getPrimaryIndexValue() to get the key.
     * If the key already exists, add to that node's duplicates list.
     */
    public void insert(FoundPerson person) {
        double key = person.getDnaProfile().getPrimaryIndexValue();

        if (root == null) {
            root = new Node(key, person);
            size++;
            return;
        }

        Node current = root;
        while (true) {
            if (key < current.key) {
                if (current.left == null) {
                    current.left = new Node(key, person);
                    size++;
                    return;
                }
                current = current.left;
            } else if (key > current.key) {
                if (current.right == null) {
                    current.right = new Node(key, person);
                    size++;
                    return;
                }
                current = current.right;
            } else {
                current.duplicates.add(person);
                size++;
                return;
            }
        }
    }



    /**
     * Find all persons whose primary index key matches exactly.
     * Return the node's person plus all duplicates, or an empty list if not found.
     */
    public List<FoundPerson> findExact(double key) {
        Node current = root;

        while (current != null) {
            if (key < current.key) {
                current = current.left;
            } else if (key > current.key) {
                current = current.right;
            } else {
                List<FoundPerson> result = new ArrayList<>(1 + current.duplicates.size());
                result.add(current.person);
                result.addAll(current.duplicates);
                return result;
            }
        }

        return List.of();
    }

    /**
     * Find all persons whose primary index key falls within [low, high] inclusive.
     * This range query should leverage BST structure to prune branches —
     * don't visit subtrees that can't contain values in range.
     */
    public List<FoundPerson> findInRange(double low, double high) {
        List<FoundPerson> result = new ArrayList<>();
        rangeDFS(root, low, high, result);
        return result;
    }

    private void rangeDFS(Node node, double low, double high, List<FoundPerson> out) {
        if (node == null) return;

        if (low < node.key) {
            rangeDFS(node.left, low, high, out);
        }

        if (low <= node.key && node.key <= high) {
            out.add(node.person);
            out.addAll(node.duplicates);
        }

        if (node.key < high) {
            rangeDFS(node.right, low, high, out);
        }
    }

    /**
     * In-order traversal returning all persons sorted by key.
     */
    public List<FoundPerson> inOrder() {
        List<FoundPerson> result = new ArrayList<>(size);
        inOrderDFS(root, result);
        return result;
    }

    private void inOrderDFS(Node node, List<FoundPerson> out) {
        if (node == null) return;
        inOrderDFS(node.left, out);
        out.add(node.person);
        out.addAll(node.duplicates);
        inOrderDFS(node.right, out);
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    /**
     * Return the height of the tree (-1 for empty tree).
     */
    public int height() {
        return height(root);
    }

    private int height(Node node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
