# CPSC-215 Project: Earthquake Disaster Response System

## Overview

On February 6, 2023, a devastating earthquake struck Turkey and Syria, killing over 50,000 people. In the aftermath, rescue teams recovered thousands of victims — some alive, some deceased. Families desperately searched for missing loved ones and pets. Identifying victims and matching them to missing person reports required processing DNA samples, GPS data, and triage decisions under extreme time pressure.

In this project, you will build the core matching engine for an earthquake disaster response system. Your program processes two streams of incoming data:

1. **Found Person/Pet reports** — rescue teams log recovered individuals with DNA profiles (STR loci), GPS coordinates, alive/dead status, and species
2. **Missing Person/Pet reports** — families submit DNA reference samples for their missing loved ones and pets

Your engine must efficiently match found persons to missing reports using DNA comparison, filter out contaminated samples (yeast, mushroom DNA), triage victims by priority, and compute the geographic disaster zone from GPS data.

**Setting:** Istanbul, Turkey (41.0082°N, 28.9784°E)

## How It Works

### The Main Loop

Your program processes an interleaved event stream — found person reports and missing reports arrive one at a time in unpredictable order. You cannot batch-process everything at the end; you must update your data structures incrementally as each event arrives.

```
while (events remain):
    event = next event

    if event is FOUND PERSON:
        1. Filter: is this human/pet DNA or noise (yeast/mushroom)?
        2. Triage: add to priority queue (alive before deceased, children before adults)
        3. Index: add DNA profile to search tree and HashMap index
        4. GPS: update running disaster zone calculation
        5. Match: check against existing missing reports

    if event is MISSING REPORT:
        1. Enqueue: add to FIFO report queue
        2. Match: search indexed found persons for DNA match candidates

    Update running statistics

Output: matches, unmatched persons, disaster zone, statistics
```

### DNA Model: STR Loci Profiles

Real forensic DNA identification uses **Short Tandem Repeats (STRs)** — the same system used by the FBI's CODIS database. Each person has a profile of 15-20 loci, each with two numeric allele values (one inherited from each parent):

```
Person: FP-0042
Locus        Allele1  Allele2
D3S1358      15       17
TH01          6        9
D21S11       29       30
FGA          22       24
...15 more loci...
```

**Matching rules:**
- Compare allele values at each shared locus
- **Direct match** (≥85% alleles match): same person found
- **Family match** (40-60% alleles match): parent/child/sibling found
- **No match** (<30%): unrelated individuals
- **Degraded samples**: some loci may have null values (damaged in earthquake conditions)
- **Organism filtering**: yeast and mushroom samples have completely different locus names — detect and discard them using Set intersection

### Species Detection

| Species | Example Loci |
|---------|-------------|
| Human | D3S1358, TH01, D21S11, FGA, vWA, CSF1PO, ... |
| Dog | FH2054, PEZ01, PEZ03, AHT121, ... |
| Cat | FCA441, FCA723, FCA740, FCA742, ... |
| Yeast (noise) | YOR100C, YDR210W, YGL115W, ... |
| Mushroom (noise) | AG_SSR01, AG_SSR02, AB_STR01, ... |

To identify species: compute the Set intersection of a sample's locus names with each known species locus set. The species with the highest overlap is the match.

## Data Structures You Must Implement

Each data structure is enforced by an interface with unit tests. You cannot bypass them.

| Data Structure | Class | Purpose |
|---|---|---|
| **BST** | `CandidateTree` | Index found persons by DNA primary allele value. Range queries narrow candidates from thousands to dozens. |
| **HashMap** | `LocusIndex` | Per-locus allele → person index. O(1) lookup: "who has allele 15 at D3S1358?" |
| **Max-Heap** | `TriageQueue` | Priority queue for found persons. Alive + children dequeued first. |
| **FIFO Queue** | `ReportQueue` | Missing reports processed in submission order. Linked list implementation. |
| **Stack** | `MatchStack` | Backtracking during multi-locus DNA comparison. Push/pop candidate states. |
| **Linked List** | `EvidenceChain` | Audit trail for each confirmed match — which loci matched, confidence at each step. |
| **Graph + BFS** | `LocationGraph` | GPS proximity graph. BFS finds clusters of nearby found persons (potential families). |
| **Set** | (java.util) | Locus name intersection for species filtering. Tracking unmatched reports. |

## Inputs (Provided — Do Not Modify)

Three JSON files per dataset:

- `found-persons-{size}.json` — array of found person records
- `missing-reports-{size}.json` — array of missing person reports
- `events-{size}.json` — interleaved event stream

Two dataset sizes:
- **Small** (~85 found, ~40 missing, ~125 events) — for development
- **Large** (~6,600 found, ~3,000 missing, ~9,600 events) — for final testing

## Outputs

### Console Report
```
=== EARTHQUAKE DISASTER RESPONSE REPORT ===

DISASTER ZONE:
  Centroid:     (41.0146, 28.9761)
  Bounding Box: (40.9040, 28.8408) to (41.1482, 29.0941)
  Area:         576.98 km^2

STATISTICS:
  Events processed:    125
  Found alive:         48
  Found deceased:      22
  Noise filtered:      15
  Matches found:       26
  Unmatched missing:   14
  Unmatched found:     44

MATCHES:
  FP-0001 <-> MR-0001  (Cem Yıldırım, confidence: 94.4%, type: direct, status: alive)
  ...
```

### results.json
Written to the data directory. Contains matches with GPS coordinates, unmatched persons, and disaster zone geometry. Used by the map visualization.

### Map Visualization
Open `src/main/resources/map/map.html` in a browser and load your `results.json`. The map displays:
- Green markers: matched persons (found ↔ missing)
- Red markers: unmatched found persons
- Blue rectangle: computed disaster zone boundary
- Orange dot: computed centroid

As your matching engine improves, you'll see more markers turn from red to green.

## Project Milestones

Use this week (week 0) to download the project, get it compiling, read the code, and understand the data files.

| Week | Focus | Classes to Implement | Deliverable |
|------|-------|---------------------|------------|
| 1 | Filtering + indexing | `OrganismFilter`, `CandidateTree`, `LocusIndex` | Filter noise DNA (Set intersection), index profiles in BST and HashMap, look up candidates by allele value |
| 2 | Matching + core data structures | `DnaComparator`, `MatchStack`, `EvidenceChain`, `ReportQueue` | DNA confidence scoring, stack-based backtracking, linked list evidence chain, FIFO queue |
| 3 | Triage + main loop + GPS | `TriageQueue`, `MatchEngine`, `DisasterZoneCalculator` | Heap-based priority queue, full event processing pipeline, disaster zone centroid and bounding box |
| 4 | Graph + polish + final submission | `LocationGraph` (BFS), large dataset testing, architecture document | Proximity clustering, performance on large dataset, architecture doc with AI usage write-up |

## What You Implement vs. What's Provided

### What's already done for you

The project is a complete, working application with everything wired together — except for the core data structures and algorithms. You do not need to worry about:

- **JSON parsing** — all file reading and writing is handled by `EventFileParser` and `ResultWriter`. Your code receives ready-to-use Java objects.
- **Data model** — all classes like `FoundPerson`, `MissingReport`, `DnaProfile`, `GpsCoordinate`, etc. are fully implemented with Jackson annotations for serialization.
- **Map visualization** — the Leaflet.js map page reads your `results.json` and renders everything automatically. No web code to write.
- **Application wiring** — `App.java` handles command-line arguments, loads data, calls your engine, writes output, and optionally launches the map server.
- **Test suite** — 68 JUnit tests are provided. They test each data structure in isolation and the full pipeline end-to-end. Run them often to track your progress.
- **Datasets** — small and large JSON files with realistic DNA profiles, GPS coordinates, and interleaved events are ready to use.

Your job is to implement the data structures and algorithms that make the engine work.

### Provided — do NOT modify:
- All model classes (`model/` package)
- `EventFileParser`, `ResultWriter`, and `MapServer` (`io/` package)
- `App.java` (entry point)
- All JSON data files (`src/main/resources/data/`)
- `map.html` visualization (`src/main/resources/map/`)
- All unit tests and integration tests (`src/test/`)
- `build.gradle.kts` and `settings.gradle.kts`

**Important:** The unit tests and data files define the project specification. Do not modify them. Your implementations must pass the tests as written. If a test fails, the issue is in your code, not the test.

### You implement:

**`structures/`** package:
1. `CandidateTree` — BST insert, search, range query, traversal
2. `LocusIndex` — HashMap-based multi-locus candidate lookup
3. `TriageQueue` — Array-based max-heap priority queue
4. `ReportQueue` — Linked list FIFO queue
5. `MatchStack` — Linked list stack for backtracking
6. `EvidenceChain` — Linked list for match evidence
7. `LocationGraph` — Adjacency list graph with BFS

**`engine/`** package:
8. `OrganismFilter` — Set intersection for species identification
9. `DnaComparator` — Multi-locus confidence scoring algorithm
10. `DisasterZoneCalculator` — Running centroid and bounding box
11. `MatchEngine` — Main loop wiring everything together

## Architecture Document Requirement

Since you are expected to use AI coding tools, you must submit a **2-3 page architecture document** describing:

1. **System Architecture** — How the components connect. Include a diagram.
2. **Data Structure Choices** — For each data structure, explain *why* it was chosen and what the Big-O complexity is for key operations.
3. **Algorithm Walkthrough** — Step-by-step explanation of how DNA matching works in your implementation.
4. **AI Usage** — Which AI tools you used, what prompts you gave, what code the AI generated vs. what you wrote/modified yourself, and any bugs the AI introduced that you had to fix.
5. **Performance Analysis** — Processing time and match results on the large dataset.

## Expected Results

Your implementation is correct when your output matches these numbers. Processing time will vary by machine — it is not graded for exact value, but your large dataset must complete.

### Small Dataset

| Statistic | Expected Value |
|---|---|
| Events processed | 125 |
| Found alive | 47 |
| Found deceased | 23 |
| Noise filtered | 15 |
| Matches found | 28 |
| Unmatched missing | 12 |
| Unmatched found | 42 |
| Centroid | (41.0593, 28.9766) |
| Bounding box | (40.9771, 28.8204) to (41.1708, 29.1346) |
| Disaster zone area | 567.28 km² |

### Large Dataset

| Statistic | Expected Value |
|---|---|
| Events processed | 9,600 |
| Found alive | 3,754 |
| Found deceased | 1,646 |
| Noise filtered | 1,200 |
| Matches found | 2,919 |
| Unmatched missing | 81 |
| Unmatched found | 2,481 |
| Centroid | (41.0592, 28.9815) |
| Bounding box | (40.9700, 28.8004) to (41.1799, 29.1997) |
| Disaster zone area | 781.21 km² |
| Processing time | varies (reference: ~20 seconds) |

## Grading

| Component | Weight |
|---|---|
| Data structure implementations (unit tests pass) | 40% |
| Matching engine correctness (integration tests) | 20% |
| GPS disaster zone computation | 10% |
| Performance on large dataset (must complete) | 10% |
| Architecture document + AI usage write-up | 15% |
| Code quality | 5% |
| **Bonus:** fastest processing time on large dataset | +5% |

The performance bonus goes to the top 3 fastest implementations on the large dataset (as measured by `processingTimeNanos` in results.json). Your solution must produce correct results to qualify.

## Getting Started

1. Download the project zip file from this Moodle assignment page.
2. Extract the zip into a local project folder (e.g., `disaster-response/`).
3. Open the project in your IDE (IntelliJ recommended — it will detect the Gradle project automatically).
4. **Mac/Linux:** you may need to make the Gradle wrapper executable: `chmod +x gradlew`
5. Verify it compiles: `./gradlew compileJava`
6. Run the tests: `./gradlew test` — you should see **58 failures**. That's expected. Each test you make pass is progress.
7. Look through the `// TODO: implement` methods in `structures/` and `engine/` — that's your work.

> **Windows users:** use `gradlew.bat` instead of `./gradlew` for all commands (e.g., `gradlew.bat test`).

## Building and Running

```bash
# Compile
./gradlew compileJava

# Run tests
./gradlew test

# Run with small dataset
./gradlew run --args="src/main/resources/data small"

# Run with small dataset + open map in browser
./gradlew run --args="src/main/resources/data small map"

# Run with large dataset
./gradlew run --args="src/main/resources/data large"

# Run with large dataset + open map in browser
./gradlew run --args="src/main/resources/data large map"

# Open map for most recent results (without re-running)
./gradlew map
```

The `map` argument starts a local web server and opens the map visualization in your browser showing matched/unmatched persons, the disaster zone boundary, and the computed centroid.

> **Note:** You may see Java native-access warnings from Gradle on Java 25. These can be safely ignored.

## Resources

- [FBI CODIS STR Loci](https://www.fbi.gov/how-we-can-help-you/dna-fingerprint-act-of-2005-expungement-policy/codis-and-ndis-fact-sheet)
- [NIST STRBase](https://strbase.nist.gov/) — allele frequency data
- [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula) — GPS distance calculation
- [Jackson JSON Library](https://github.com/FasterXML/jackson)
