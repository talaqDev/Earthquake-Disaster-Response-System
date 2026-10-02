package edu.trinity.cpsc215.disaster.model;

import java.util.List;

public class MatchResult {
    private final String foundPersonId;
    private final String missingReportId;
    private final double confidence;
    private final String matchType;
    private final List<EvidenceNode> evidenceChain;

    public MatchResult(String foundPersonId, String missingReportId,
                       double confidence, String matchType,
                       List<EvidenceNode> evidenceChain) {
        this.foundPersonId = foundPersonId;
        this.missingReportId = missingReportId;
        this.confidence = confidence;
        this.matchType = matchType;
        this.evidenceChain = evidenceChain;
    }

    public String getFoundPersonId() { return foundPersonId; }
    public String getMissingReportId() { return missingReportId; }
    public double getConfidence() { return confidence; }
    public String getMatchType() { return matchType; }
    public List<EvidenceNode> getEvidenceChain() { return evidenceChain; }

    @Override
    public String toString() {
        return String.format("Match[%s <-> %s, %.1f%% %s]",
                foundPersonId, missingReportId, confidence * 100, matchType);
    }
}
