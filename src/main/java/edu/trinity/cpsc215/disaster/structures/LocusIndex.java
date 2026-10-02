package edu.trinity.cpsc215.disaster.structures;

import edu.trinity.cpsc215.disaster.model.DnaProfile;
import edu.trinity.cpsc215.disaster.model.FoundPerson;

import java.util.*;

/**
 * HashMap-based index for fast DNA candidate lookup.
 * For each locus, maintains a map from allele value to list of persons
 * who have that allele value at that locus.
 *
 * Structure: locus name -> (allele value -> list of persons with that allele)
 */
public class LocusIndex {

    private final Map<String, Map<Double, List<FoundPerson>>> index;

    public LocusIndex() {
        this.index = new HashMap<>();
    }

    /**
     * Index a found person's DNA profile. For each locus and each non-null
     * allele value, add the person to the corresponding bucket.
     *
     * Hint: use computeIfAbsent() to create maps/lists on demand.
     */
    public void addPerson(FoundPerson person) {
        DnaProfile profile = person.getDnaProfile();
        if (profile == null) return;

        Map<String, DnaProfile.AllelePair> loci = profile.getLoci();
        if (loci == null) return;

        for (Map.Entry<String, DnaProfile.AllelePair> entry : loci.entrySet()) {
            String locus = entry.getKey();
            DnaProfile.AllelePair pair = entry.getValue();
            if (pair == null) continue;

            Map<Double, List<FoundPerson>> alleleMap = index.computeIfAbsent(
                    locus, k -> new HashMap<>(16)
            );

            Double a1 = pair.allele1();
            if (a1 != null) {
                alleleMap.computeIfAbsent(a1, k -> new ArrayList<>(4)).add(person);
            }

            Double a2 = pair.allele2();
            if (a2 != null && !Objects.equals(a1, a2)) {
                alleleMap.computeIfAbsent(a2, k -> new ArrayList<>(4)).add(person);
            }
        }
    }

    /**
     * Find candidate persons who have the given allele value at the given locus.
     * Return empty list if no matches.
     */
    public List<FoundPerson> findCandidates(String locus, Double alleleValue) {
        if (locus == null || alleleValue == null) return List.of();

        Map<Double, List<FoundPerson>> alleleMap = index.get(locus);
        if (alleleMap == null) return List.of();

        List<FoundPerson> bucket = alleleMap.get(alleleValue);
        return (bucket != null) ? bucket : List.of();
    }

    /**
     * Find candidate persons who share any allele at a given locus with the query alleles.
     * Returns the union of candidates for both allele values.
     */
    public Set<FoundPerson> findCandidatesForLocus(String locus, DnaProfile.AllelePair queryAlleles) {
        if (locus == null || queryAlleles == null) return new HashSet<>();

        Map<Double, List<FoundPerson>> alleleMap = index.get(locus);
        if (alleleMap == null) return new HashSet<>();

        Set<FoundPerson> candidates = new HashSet<>(16);
        Double a1 = queryAlleles.allele1();

        if (a1 != null) {
            List<FoundPerson> bucket = alleleMap.get(a1);
            if (bucket != null) candidates.addAll(bucket);
        }

        Double a2 = queryAlleles.allele2();
        if (a2 != null && !a2.equals(a1)) {
            List<FoundPerson> bucket = alleleMap.get(a2);
            if (bucket != null) candidates.addAll(bucket);
        }

        return candidates;
    }

    /**
     * Find candidate persons who share alleles across multiple loci.
     * Returns the intersection of candidate sets from each locus.
     * Stop early if the intersection becomes empty after checking minLociMatch loci.
     */
    public Set<FoundPerson> findCandidatesMultiLocus(DnaProfile queryProfile, int minLociMatch) {
        if (queryProfile == null) return new HashSet<>();

        Map<String, DnaProfile.AllelePair> loci = queryProfile.getLoci();
        if (loci == null || loci.isEmpty()) return new HashSet<>();

        List<Set<FoundPerson>> candidateSets = new ArrayList<>(loci.size());

        for (Map.Entry<String, DnaProfile.AllelePair> entry : loci.entrySet()) {
            String locus = entry.getKey();
            DnaProfile.AllelePair pair = entry.getValue();

            if (!index.containsKey(locus)) continue;

            Set<FoundPerson> locusSet = findCandidatesForLocus(locus, pair);
            if (!locusSet.isEmpty()) {
                candidateSets.add(locusSet);
            }
        }

        if (candidateSets.isEmpty()) return new HashSet<>();

        candidateSets.sort((a, b) -> Integer.compare(a.size(), b.size()));
        Set<FoundPerson> result = new HashSet<>(candidateSets.get(0));

        for (int i = 1; i < candidateSets.size(); i++) {
            result.retainAll(candidateSets.get(i));

            if (result.isEmpty()) return result;
        }

        return result;
    }

    public Set<String> getIndexedLoci() {
        return index.keySet();
    }

    public int size() {
        return index.values().stream()
                .mapToInt(m -> m.values().stream().mapToInt(List::size).sum())
                .sum();
    }
}
