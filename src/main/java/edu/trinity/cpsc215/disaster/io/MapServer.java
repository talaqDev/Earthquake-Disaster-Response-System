package edu.trinity.cpsc215.disaster.io;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple HTTP server to serve the map visualization.
 * Serves map.html and results.json from the appropriate directories.
 */
public class MapServer {

    private static final int PORT = 8000;

    public static void start(Path mapHtml, Path resultsJson) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/map.html", exchange -> serveFile(exchange, mapHtml, "text/html"));
        server.createContext("/results.json", exchange -> serveFile(exchange, resultsJson, "application/json"));
        server.createContext("/", exchange -> {
            // Redirect root to map.html
            exchange.getResponseHeaders().set("Location", "/map.html");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.setExecutor(null);
        server.start();

        String url = "http://localhost:" + PORT + "/map.html";
        System.out.println("Map server running at " + url);
        System.out.println("Press Ctrl+C to stop.");
        System.out.println();

        // Try to open browser automatically
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                // WSL or Linux — try Windows browser via cmd.exe, fall back to xdg-open
                try {
                    Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start", url});
                } catch (IOException e) {
                    try {
                        Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                    } catch (IOException ignored) {
                        System.out.println("Open " + url + " in your browser.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Open " + url + " in your browser.");
        }
    }

    private static void serveFile(HttpExchange exchange, Path file, String contentType) throws IOException {
        if (!Files.exists(file)) {
            String msg = "File not found: " + file;
            exchange.sendResponseHeaders(404, msg.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(msg.getBytes());
            }
            return;
        }

        byte[] data = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    public static void main(String[] args) throws IOException {
        Path mapHtml = Path.of("src/main/resources/map/map.html");
        Path resultsJson = Path.of("src/main/resources/data/results.json");

        if (args.length >= 1) resultsJson = Path.of(args[0]);
        if (args.length >= 2) mapHtml = Path.of(args[1]);

        start(mapHtml, resultsJson);
    }
}
