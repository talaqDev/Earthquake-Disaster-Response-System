package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DnaProfile;
import edu.trinity.cpsc215.disaster.model.DnaProfile.AllelePair;
import edu.trinity.cpsc215.disaster.model.EvidenceNode;
import edu.trinity.cpsc215.disaster.structures.EvidenceChain;
import edu.trinity.cpsc215.disaster.structures.MatchStack;

import java.util.*;

/**
 * Compares two DNA profiles and computes a confidence score.
 * Uses a stack-based approach for backtracking through locus comparisons.
 *
 * Matching rules:
 * - For each shared locus, compare allele values
 * - An allele matches if the values are equal (within ALLELE_TOLERANCE)
 * - Direct match: >= 85% alleles match (same person)
 * - Family match: >= 40% alleles match (parent/child)
 * - No match: < 30%
 * - Confidence = matching alleles / total comparable alleles
 */
public class DnaComparator implements Comparator<MatchStack.CandidateState> {

    public static final double DIRECT_MATCH_THRESHOLD = 0.85;
    public static final double FAMILY_MATCH_THRESHOLD = 0.40;
    public static final double NO_MATCH_THRESHOLD = 0.30;
    private static final double ALLELE_TOLERANCE = 0.01;
    private static final int EARLY_TERMINATION_LOCI   = 5;

    /**
     * Compare two DNA profiles and return a confidence score with evidence chain.
     *
     * Algorithm:
     *   1. Find shared loci (set intersection of both profiles' locus names)
     *   2. If no shared loci, return confidence 0.0 with matchType "no_shared_loci"
     *   3. For each shared locus (sorted alphabetically):
     *      a. Count matching alleles using countAlleleMatches()
     *      b. Count comparable alleles using countComparableAlleles()
     *      c. Update running confidence = matchingAlleles / totalComparableAlleles
     *      d. Push state onto MatchStack (for backtracking visibility)
     *      e. Append an EvidenceNode to the evidence chain
     *      f. Early termination: if confidence < NO_MATCH_THRESHOLD after 5+ loci, break
     *   4. Determine matchType based on final confidence:
     *      >= DIRECT_MATCH_THRESHOLD -> "direct"
     *      >= FAMILY_MATCH_THRESHOLD -> "family"
     *      else -> "none"
     *   5. Return MatchResult with confidence, evidence chain, and match type
     */
    public MatchResult compareProfiles(DnaProfile profile1, DnaProfile profile2) {
        if (profile1 == null || profile2 == null) {
            return new MatchResult(0.0, new EvidenceChain(), "none");
        }

        Map<String, AllelePair> loci1 = profile1.getLoci();
        Map<String, AllelePair> loci2 = profile2.getLoci();

        if (loci1 == null || loci2 == null || loci1.isEmpty() || loci2.isEmpty()) {
            return new MatchResult(0.0, new EvidenceChain(), "no_shared_loci");
        }

        Map<String, AllelePair> smaller = loci1.size() <= loci2.size() ? loci1 : loci2;
        Map<String, AllelePair> larger  = loci1.size() <= loci2.size() ? loci2 : loci1;

        List<String> sharedLoci = new ArrayList<>(smaller.size());
        for (String locus : smaller.keySet()) {
            if (larger.containsKey(locus)) {
                sharedLoci.add(locus);
            }
        }

        if (sharedLoci.isEmpty()) {
            return new MatchResult(0.0, new EvidenceChain(), "no_shared_loci");
        }

        java.util.Collections.sort(sharedLoci);

        EvidenceChain evidenceChain = new EvidenceChain();
        MatchStack    stack         = new MatchStack();

        int totalMatchingAlleles    = 0;
        int totalComparableAlleles  = 0;
        double runningConfidence    = 0.0;

        for (int i = 0; i < sharedLoci.size(); i++) {
            String locus = sharedLoci.get(i);

            AllelePair pair1 = loci1.get(locus);
            AllelePair pair2 = loci2.get(locus);

            if (pair1 == null || pair2 == null) continue;

            int matching   = countAlleleMatches(pair1, pair2);
            int comparable = countComparableAlleles(pair1, pair2);

            if (comparable == 0) continue;

            totalMatchingAlleles   += matching;
            totalComparableAlleles += comparable;

            runningConfidence = (double) totalMatchingAlleles / totalComparableAlleles;

            stack.push(new MatchStack.CandidateState(
                    null,
                    i + 1,
                    totalMatchingAlleles,
                    runningConfidence
            ));

            evidenceChain.append(new EvidenceNode(
                    locus,
                    matching > 0,
                    runningConfidence,
                    formatAlleles(pair1) + " vs " + formatAlleles(pair2)
            ));

            if (i + 1 >= EARLY_TERMINATION_LOCI
                    && runningConfidence < NO_MATCH_THRESHOLD) {
                break;
            }
        }

        String matchType;
        if      (runningConfidence >= DIRECT_MATCH_THRESHOLD) matchType = "direct";
        else if (runningConfidence >= FAMILY_MATCH_THRESHOLD) matchType = "family";
        else                                                   matchType = "none";

        return new MatchResult(runningConfidence, evidenceChain, matchType);
    }

    /**
     * Count how many alleles match between two allele pairs.
     * Each allele in pair A can match at most one allele in pair B.
     * Use ALLELE_TOLERANCE for floating-point comparison.
     *
     * Example: A=[15, 17] vs B=[15, 18] -> 1 match (the 15s)
     * Example: A=[15, 17] vs B=[15, 17] -> 2 matches
     * Example: A=[15, 17] vs B=[12, 14] -> 0 matches
     *
     * Be careful not to double-count: if A.allele1 matches B.allele1,
     * then A.allele2 cannot also match B.allele1.
     */
    private int countAlleleMatches(AllelePair a, AllelePair b) {
        Double a1 = a.allele1();
        Double a2 = a.allele2();
        Double b1 = b.allele1();
        Double b2 = b.allele2();

        int matches = 0;
        boolean b1Used = false;
        boolean b2Used = false;

        if (a1 != null) {
            if (b1 != null && !b1Used && Math.abs(a1 - b1) <= ALLELE_TOLERANCE) {
                matches++;
                b1Used = true;
            } else if (b2 != null && !b2Used && Math.abs(a1 - b2) <= ALLELE_TOLERANCE) {
                matches++;
                b2Used = true;
            }
        }

        if (a2 != null) {
            if (b1 != null && !b1Used && Math.abs(a2 - b1) <= ALLELE_TOLERANCE) {
                matches++;
                b1Used = true;
            } else if (b2 != null && !b2Used && Math.abs(a2 - b2) <= ALLELE_TOLERANCE) {
                matches++;
            }
        }

        return matches;
    }

    /**
     * Count how many alleles are comparable (both sides have non-null values).
     * An allele in A is comparable if at least one allele in B is non-null.
     */
    private int countComparableAlleles(AllelePair a, AllelePair b) {
        boolean bHasAny = (b.allele1() != null || b.allele2() != null);
        if (!bHasAny) return 0;

        int count = 0;
        if (a.allele1() != null) count++;
        if (a.allele2() != null) count++;
        return count;
    }

    private String formatAlleles(AllelePair pair) {
        return String.format("[%s, %s]",
                pair.allele1() != null ? pair.allele1().toString() : "null",
                pair.allele2() != null ? pair.allele2().toString() : "null");
    }

    /**
     * Comparator: rank candidate states by confidence (highest first).
     */
    @Override
    public int compare(MatchStack.CandidateState a, MatchStack.CandidateState b) {
        return Double.compare(b.currentConfidence(), a.currentConfidence());
    }

    /**
     * Inner result class for profile comparison.
     */
    public record MatchResult(double confidence, EvidenceChain evidence, String matchType) {
    }
}
