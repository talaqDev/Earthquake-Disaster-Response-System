package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A DNA profile consisting of STR loci, each with two allele values.
 * Null allele values represent degraded/missing data.
 */
public class DnaProfile {

    public record AllelePair(Double allele1, Double allele2) {

        public boolean hasData() {
                return allele1 != null || allele2 != null;
            }
    }

    private final Map<String, AllelePair> loci;

    @JsonCreator
    public DnaProfile(Map<String, AllelePair> loci) {
        this.loci = loci != null ? loci : Map.of();
    }

    @JsonValue
    public Map<String, AllelePair> getLoci() {
        return Collections.unmodifiableMap(loci);
    }

    public Set<String> getLocusNames() {
        return loci.keySet();
    }

    public AllelePair getAlleles(String locus) {
        return loci.get(locus);
    }

    public int getLocusCount() {
        return loci.size();
    }

    /**
     * Returns the primary allele value used for tree indexing.
     * Uses the first allele of the first locus alphabetically.
     */
    public double getPrimaryIndexValue() {
        return loci.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(e -> e.getValue().allele1() != null)
                .mapToDouble(e -> e.getValue().allele1())
                .findFirst()
                .orElse(0.0);
    }
}
