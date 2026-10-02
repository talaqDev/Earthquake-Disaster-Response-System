package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DisasterZone;
import edu.trinity.cpsc215.disaster.model.GpsCoordinate;

/**
 * Calculates the disaster zone from GPS coordinates of found persons.
 * Maintains running totals for incremental centroid calculation.
 * Tracks bounding box (min/max lat/lon) and computes area.
 *
 * This class is called once per found-person event, so it must
 * update incrementally — not recompute from scratch each time.
 */
public class DisasterZoneCalculator {

    private double sumLat;
    private double sumLon;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private int count;

    public DisasterZoneCalculator() {
        sumLat = 0;
        sumLon = 0;
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;
        count = 0;
    }

    /**
     * Add a GPS coordinate to the running calculation.
     * Update: sumLat, sumLon, minLat, maxLat, minLon, maxLon, count.
     */
    public void addCoordinate(GpsCoordinate coord) {
        if (coord == null) return;

        double lat = coord.getLatitude();
        double lon = coord.getLongitude();

        sumLat += lat;
        sumLon += lon;

        if (lat < minLat) minLat = lat;
        if (lat > maxLat) maxLat = lat;
        if (lon < minLon) minLon = lon;
        if (lon > maxLon) maxLon = lon;

        count++;
    }

    /**
     * Get the current centroid (average lat, average lon).
     * Return null if no coordinates have been added.
     */
    public GpsCoordinate getCentroid() {
        if (count == 0) return null;
        return new GpsCoordinate(sumLat / count, sumLon / count);
    }

    /**
     * Get the bounding box minimum corner (SW): (minLat, minLon).
     */
    public GpsCoordinate getBoundingBoxMin() {
        if (count == 0) return null;
        return new GpsCoordinate(minLat, minLon);
    }

    /**
     * Get the bounding box maximum corner (NE): (maxLat, maxLon).
     */
    public GpsCoordinate getBoundingBoxMax() {
        if (count == 0) return null;
        return new GpsCoordinate(maxLat, maxLon);
    }

    /**
     * Calculate the approximate area of the bounding box in km^2.
     *
     * Steps:
     *   1. Compute height in km: distance from (minLat, minLon) to (maxLat, minLon)
     *   2. Compute width in km: distance from (midLat, minLon) to (midLat, maxLon)
     *      where midLat = (minLat + maxLat) / 2
     *   3. Area = height * width
     *
     * Use GpsCoordinate.distanceKm() for distance calculations.
     */
    public double getAreaKmSquared() {
        if (count == 0) return 0.0;

        GpsCoordinate swCorner  = new GpsCoordinate(minLat, minLon);
        GpsCoordinate nwCorner  = new GpsCoordinate(maxLat, minLon);

        double midLat = (minLat + maxLat) / 2.0;
        GpsCoordinate midWest   = new GpsCoordinate(midLat, minLon);
        GpsCoordinate midEast   = new GpsCoordinate(midLat, maxLon);

        double height = swCorner.distanceKm(nwCorner);
        double width  = midWest.distanceKm(midEast);

        return height * width;
    }

    /**
     * Build the complete DisasterZone result object.
     */
    public DisasterZone calculate() {
        if (count == 0) return null;

        return new DisasterZone(
                getCentroid(),
                getBoundingBoxMin(),
                getBoundingBoxMax(),
                getAreaKmSquared()
        );
    }

    public int getCount() { return count; }
}
