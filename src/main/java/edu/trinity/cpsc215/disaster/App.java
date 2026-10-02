package edu.trinity.cpsc215.disaster;

import edu.trinity.cpsc215.disaster.engine.MatchEngine;
import edu.trinity.cpsc215.disaster.io.EventFileParser;
import edu.trinity.cpsc215.disaster.io.MapServer;
import edu.trinity.cpsc215.disaster.io.ResultWriter;
import edu.trinity.cpsc215.disaster.model.Event;
import edu.trinity.cpsc215.disaster.model.FoundPerson;
import edu.trinity.cpsc215.disaster.model.MissingReport;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void main(String[] args) {
        try {
            String dataDir = args.length > 0 ? args[0] : "src/main/resources/data";
            String size = args.length > 1 ? args[1] : "small";
            boolean openMap = Arrays.asList(args).contains("map");

            String suffix = "-" + size + ".json";
            Path foundPath = Path.of(dataDir, "found-persons" + suffix);
            Path missingPath = Path.of(dataDir, "missing-reports" + suffix);
            Path eventsPath = Path.of(dataDir, "events" + suffix);

            System.out.printf("Loading %s dataset from %s...%n", size, dataDir);

            EventFileParser parser = new EventFileParser();
            List<FoundPerson> foundPersons = parser.parseFoundPersons(foundPath);
            List<MissingReport> missingReports = parser.parseMissingReports(missingPath);
            List<Event> events = parser.parseEvents(eventsPath);

            System.out.printf("Loaded: %d found persons, %d missing reports, %d events%n",
                    foundPersons.size(), missingReports.size(), events.size());
            System.out.println();

            // Run the engine
            MatchEngine engine = new MatchEngine();
            engine.loadData(foundPersons, missingReports);

            long startTime = System.currentTimeMillis();
            engine.processEvents(events);
            long elapsed = System.currentTimeMillis() - startTime;

            // Output results
            ResultWriter writer = new ResultWriter();
            writer.printConsoleReport(engine);

            Path outputPath = Path.of(dataDir, "results.json");
            writer.writeResultsJson(engine, outputPath);
            System.out.printf("Results written to %s%n", outputPath);
            System.out.printf("Processing time: %d ms%n", elapsed);

            // Optionally launch map server
            if (openMap) {
                System.out.println();
                MapServer.start(
                        Path.of("src/main/resources/map/map.html"),
                        outputPath
                );
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
