package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.*;
import edu.trinity.cpsc215.disaster.structures.*;

import java.util.*;

/**
 * The main event processing engine.
 * Processes a stream of Found and Missing events, maintaining data structures
 * for indexing, matching, triaging, and computing disaster zone statistics.
 *
 * This class wires together all the data structures you've built.
 * The processEvents() method and loadData() are provided.
 * You implement the per-event processing methods.
 */
public class MatchEngine {

    private final CandidateTree candidateTree;
    private final LocusIndex locusIndex;
    private final TriageQueue triageQueue;
    private final ReportQueue reportQueue;
    private final LocationGraph locationGraph;
    private final DisasterZoneCalculator zoneCalculator;
    private final OrganismFilter organismFilter;
    private final DnaComparator dnaComparator;

    private final Map<String, FoundPerson> foundPersonsById;
    private final Map<String, MissingReport> missingReportsById;

    private final List<MatchResult> matches;
    private final Set<String> matchedFoundIds;
    private final Set<String> matchedMissingIds;

    private int totalEventsProcessed;
    private int filteredNoiseCount;
    private int foundAliveCount;
    private int foundDeceasedCount;
    private long processingTimeNanos;

    public MatchEngine() {
        this.candidateTree = new CandidateTree();
        this.locusIndex = new LocusIndex();
        this.triageQueue = new TriageQueue();
        this.reportQueue = new ReportQueue();
        this.locationGraph = new LocationGraph();
        this.zoneCalculator = new DisasterZoneCalculator();
        this.organismFilter = new OrganismFilter();
        this.dnaComparator = new DnaComparator();

        this.foundPersonsById = new HashMap<>();
        this.missingReportsById = new HashMap<>();

        this.matches = new ArrayList<>();
        this.matchedFoundIds = new HashSet<>();
        this.matchedMissingIds = new HashSet<>();

        this.totalEventsProcessed = 0;
        this.filteredNoiseCount = 0;
        this.foundAliveCount = 0;
        this.foundDeceasedCount = 0;
    }

    /** Load reference data before processing events. (PROVIDED — do not modify) */
    public void loadData(List<FoundPerson> foundPersons, List<MissingReport> missingReports) {
        for (FoundPerson fp : foundPersons) {
            foundPersonsById.put(fp.getId(), fp);
        }
        for (MissingReport mr : missingReports) {
            missingReportsById.put(mr.getId(), mr);
        }
    }

    /** Process the event stream. This is the main loop. (PROVIDED — do not modify) */
    public void processEvents(List<Event> events) {
        long startNanos = System.nanoTime();

        for (Event event : events) {
            processEvent(event);
            totalEventsProcessed++;
        }

        processRemainingReports();

        processingTimeNanos = System.nanoTime() - startNanos;

        locationGraph.buildEdges(1.0);
    }

    /** Route an event to the appropriate handler. (PROVIDED — do not modify) */
    public void processEvent(Event event) {
        switch (event.getType()) {
            case FOUND -> processFoundEvent(event);
            case MISSING -> processMissingEvent(event);
        }
    }

    /**
     * Process a FOUND person event. Steps:
     *
     * 1. Look up the FoundPerson from foundPersonsById using event.getRefId()
     *    (return early if not found)
     * 2. FILTER: use organismFilter.isTargetSpecies() on the person's DNA profile.
     *    If not target species, increment filteredNoiseCount and return.
     * 3. Track alive/deceased counts
     * 4. TRIAGE: enqueue into triageQueue
     * 5. INDEX: insert into candidateTree and add to locusIndex
     * 6. GPS: if person has a location, add coordinate to zoneCalculator
     *    and add vertex to locationGraph
     * 7. MATCH: call attemptMatchForFound(person)
     */
    private void processFoundEvent(Event event) {
        FoundPerson person = foundPersonsById.get(event.getRefId());
        if (person == null) return;

        if (!organismFilter.isTargetSpecies(person.getDnaProfile())) {
            filteredNoiseCount++;
            return;
        }

        if (person.getStatus() == Status.ALIVE) {
            foundAliveCount++;
        } else {
            foundDeceasedCount++;
        }

        triageQueue.enqueue(person);

        candidateTree.insert(person);
        locusIndex.addPerson(person);

        GpsCoordinate gps = person.getLocation();
        if (gps != null) {
            zoneCalculator.addCoordinate(gps);
            locationGraph.addVertex(person);
        }

        attemptMatchForFound(person);
    }

    /**
     * Process a MISSING report event. Steps:
     *
     * 1. Look up the MissingReport from missingReportsById using event.getRefId()
     *    (return early if not found)
     * 2. Enqueue into reportQueue
     * 3. Call attemptMatchForMissing(report)
     */
    private void processMissingEvent(Event event) {
        MissingReport report = missingReportsById.get(event.getRefId());
        if (report == null) return;

        reportQueue.enqueue(report);
        attemptMatchForMissing(report);
    }

    /**
     * Try to match a found person against all unmatched missing reports.
     *
     * For each missing report in missingReportsById.values():
     *   - Skip if this found person is already matched (check matchedFoundIds after each attempt)
     *   - Skip if the missing report is already matched
     *   - Skip if species don't match
     *   - Call tryMatch(person, report)
     */
    private void attemptMatchForFound(FoundPerson person) {
        for (MissingReport report : missingReportsById.values()) {
            if (matchedFoundIds.contains(person.getId())) break;

            if (matchedMissingIds.contains(report.getId())) continue;
            if (person.getSpecies() != report.getSpecies()) continue;

            tryMatch(person, report);
        }
    }

    /**
     * Try to match a missing report against indexed found persons.
     *
     * 1. Return early if report is already matched
     * 2. Use locusIndex.findCandidatesMultiLocus() to find candidates
     *    (pass the report's DNA profile and minLociMatch = 3)
     * 3. For each candidate that isn't already matched, call tryMatch()
     * 4. Break early if this report gets matched
     */
    private void attemptMatchForMissing(MissingReport report) {
        if (matchedMissingIds.contains(report.getId())) return;

        Set<FoundPerson> candidates = locusIndex.findCandidatesMultiLocus(
                report.getDnaProfile(), 3);

        if (candidates.isEmpty()) return;

        for (FoundPerson candidate : candidates) {
            if (matchedMissingIds.contains(report.getId())) break;
            if (matchedFoundIds.contains(candidate.getId())) continue;

            if (report.getSpecies() != candidate.getSpecies()) continue;

            tryMatch(candidate, report);
        }
    }

    /**
     * Attempt a DNA match between a found person and a missing report.
     *
     * 1. Use dnaComparator.compareProfiles() to compare DNA profiles
     * 2. If confidence >= FAMILY_MATCH_THRESHOLD:
     *    a. Create a MatchResult with found ID, missing ID, confidence,
     *       match type, and evidence chain (use evidence.toList())
     *    b. Add to matches list
     *    c. Add both IDs to matchedFoundIds and matchedMissingIds
     */
    private void tryMatch(FoundPerson found, MissingReport report) {
        DnaComparator.MatchResult result = dnaComparator.compareProfiles(
                found.getDnaProfile(), report.getDnaProfile());

        if (result.confidence() >= DnaComparator.FAMILY_MATCH_THRESHOLD) {
            matches.add(new MatchResult(
                    found.getId(),
                    report.getId(),
                    result.confidence(),
                    result.matchType(),
                    result.evidence().toList()
            ));
            matchedFoundIds.add(found.getId());
            matchedMissingIds.add(report.getId());
        }
    }

    /** Process any remaining reports in the queue. (PROVIDED — do not modify) */
    private void processRemainingReports() {
        while (!reportQueue.isEmpty()) {
            MissingReport report = reportQueue.dequeue();
            if (!matchedMissingIds.contains(report.getId())) {
                attemptMatchForMissing(report);
            }
        }
    }

    // ---- Getters (PROVIDED — do not modify) ----

    public List<MatchResult> getMatches() { return matches; }

    public DisasterZone getDisasterZone() { return zoneCalculator.calculate(); }

    public List<String> getUnmatchedFoundIds() {
        List<String> unmatched = new ArrayList<>();

        for (String id : foundPersonsById.keySet()) {
            if (!matchedFoundIds.contains(id)) {
                FoundPerson fp = foundPersonsById.get(id);
                if (organismFilter.isTargetSpecies(fp.getDnaProfile())) {
                    unmatched.add(id);
                }
            }
        }
        return unmatched;
    }

    public List<String> getUnmatchedMissingIds() {
        List<String> unmatched = new ArrayList<>();

        for (String id : missingReportsById.keySet()) {
            if (!matchedMissingIds.contains(id)) {
                unmatched.add(id);
            }
        }
        return unmatched;
    }

    public List<List<FoundPerson>> getLocationClusters() { return locationGraph.findClusters(); }
    public int getTotalEventsProcessed() { return totalEventsProcessed; }
    public int getFilteredNoiseCount() { return filteredNoiseCount; }
    public int getFoundAliveCount() { return foundAliveCount; }
    public int getFoundDeceasedCount() { return foundDeceasedCount; }
    public long getProcessingTimeNanos() { return processingTimeNanos; }
    public int getMatchCount() { return matches.size(); }
    public Map<String, FoundPerson> getFoundPersonsById() { return foundPersonsById; }
    public Map<String, MissingReport> getMissingReportsById() { return missingReportsById; }
}
