package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DnaProfile;
import edu.trinity.cpsc215.disaster.model.Species;

import java.util.HashSet;
import java.util.Set;

/**
 * Filters DNA profiles by organism type using Set intersection of locus names.
 * Human, dog, and cat profiles use distinct STR locus panels.
 * Yeast and mushroom profiles have completely different locus names.
 *
 * To identify a species: compute the Set intersection of the profile's locus names
 * with each known species locus set. The species with the highest overlap wins.
 */
public class OrganismFilter {

    private static final Set<String> HUMAN_LOCI = Set.of(
            "D3S1358", "TH01", "D21S11", "D18S51", "D5S818",
            "D13S317", "D7S820", "D16S539", "CSF1PO", "vWA",
            "D8S1179", "TPOX", "FGA", "D2S1338", "D19S433",
            "D1S1656", "D12S391", "D2S441", "D10S1248", "SE33"
    );

    private static final Set<String> DOG_LOCI = Set.of(
            "FH2054", "FH2010", "FH2079", "PEZ01", "PEZ03",
            "PEZ05", "PEZ06", "PEZ08", "PEZ12", "PEZ20",
            "FH2328", "FH2001", "AHT121", "AHT137", "AHTk211"
    );

    private static final Set<String> CAT_LOCI = Set.of(
            "FCA441", "FCA723", "FCA740", "FCA742", "FCA749",
            "FCA026", "FCA069", "FCA075", "FCA105", "FCA149",
            "FCA220", "FCA229", "FCA310", "FCA391", "FCA453"
    );

    private static final Set<String> YEAST_LOCI = Set.of(
            "YOR100C", "YDR210W", "YGL115W", "YMR197C", "YNL209W",
            "YPL061W", "YBR085W", "YER103W", "YGR192C", "YLR044C"
    );

    private static final Set<String> MUSHROOM_LOCI = Set.of(
            "AG_SSR01", "AG_SSR02", "AG_SSR03", "AG_SSR04", "AG_SSR05",
            "AB_STR01", "AB_STR02", "AB_STR03", "AB_STR04", "AB_STR05"
    );

    private static final Object[][] SPECIES_LOCI = {
            { Species.HUMAN,    HUMAN_LOCI    },
            { Species.DOG,      DOG_LOCI      },
            { Species.CAT,      CAT_LOCI      },
            { Species.YEAST,    YEAST_LOCI    },
            { Species.MUSHROOM, MUSHROOM_LOCI },
    };

    /**
     * Identify the species of a DNA profile by checking which known locus set
     * has the highest overlap (set intersection) with the profile's loci.
     *
     * Steps:
     *   1. Get the profile's locus names as a Set
     *   2. Compute intersection size with each species' locus set
     *   3. Return the Species with the largest intersection
     *   4. Return UNKNOWN if no overlap with any species
     *
     * Hint: to compute set intersection, copy one set and call retainAll().
     */
    public Species identifySpecies(DnaProfile profile) {
        if (profile == null) return Species.UNKNOWN;

        Set<String> profileLoci = profile.getLocusNames();
        if (profileLoci == null || profileLoci.isEmpty()) return Species.UNKNOWN;

        Species bestSpecies = Species.UNKNOWN;
        int bestCount = 0;

        for (Object[] entry : SPECIES_LOCI) {
            Species species       = (Species) entry[0];
            @SuppressWarnings("unchecked")
            Set<String> knownLoci = (Set<String>) entry[1];

            int count = 0;
            for (String locus : profileLoci) {
                if (knownLoci.contains(locus)) {
                    count++;
                }
            }

            if (count > bestCount) {
                bestCount  = count;
                bestSpecies = species;

                if (count == profileLoci.size()) break;
            }
        }

        return bestCount > 0 ? bestSpecies : Species.UNKNOWN;
    }

    /**
     * Check if a DNA profile belongs to a target species (human, dog, or cat).
     * Uses identifySpecies() and Species.isTargetSpecies().
     */
    public boolean isTargetSpecies(DnaProfile profile) {
        if (profile == null) return false;

        Set<String> profileLoci = profile.getLocusNames();
        if (profileLoci == null || profileLoci.isEmpty()) return false;

        for (String locus : profileLoci) {
            if (HUMAN_LOCI.contains(locus)) return true;
            if (DOG_LOCI.contains(locus))   return true;
            if (CAT_LOCI.contains(locus))   return true;
        }

        return false;
    }

    public static Set<String> getHumanLoci() { return HUMAN_LOCI; }
    public static Set<String> getDogLoci() { return DOG_LOCI; }
    public static Set<String> getCatLoci() { return CAT_LOCI; }
}
