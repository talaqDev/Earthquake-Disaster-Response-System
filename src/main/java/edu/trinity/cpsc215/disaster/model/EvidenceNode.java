package edu.trinity.cpsc215.disaster.model;

/**
 * A single piece of evidence in the match evidence chain.
 * Records which locus was compared and whether it matched.
 */
public class EvidenceNode {
    private final String locusName;
    private final boolean matched;
    private final double confidenceAtStep;
    private final String detail;

    public EvidenceNode(String locusName, boolean matched,
                        double confidenceAtStep, String detail) {
        this.locusName = locusName;
        this.matched = matched;
        this.confidenceAtStep = confidenceAtStep;
        this.detail = detail;
    }

    public String getLocusName() { return locusName; }
    public boolean isMatched() { return matched; }
    public double getConfidenceAtStep() { return confidenceAtStep; }
    public String getDetail() { return detail; }

    @Override
    public String toString() {
        return String.format("%s: %s (%.1f%%)",
                locusName, matched ? "MATCH" : "NO MATCH", confidenceAtStep * 100);
    }
}
