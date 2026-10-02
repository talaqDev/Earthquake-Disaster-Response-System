package edu.trinity.cpsc215.disaster.model;

public class DisasterZone {
    private final GpsCoordinate centroid;
    private final GpsCoordinate boundingBoxMin;
    private final GpsCoordinate boundingBoxMax;
    private final double areaKmSquared;

    public DisasterZone(GpsCoordinate centroid,
                        GpsCoordinate boundingBoxMin,
                        GpsCoordinate boundingBoxMax,
                        double areaKmSquared) {
        this.centroid = centroid;
        this.boundingBoxMin = boundingBoxMin;
        this.boundingBoxMax = boundingBoxMax;
        this.areaKmSquared = areaKmSquared;
    }

    public GpsCoordinate getCentroid() { return centroid; }
    public GpsCoordinate getBoundingBoxMin() { return boundingBoxMin; }
    public GpsCoordinate getBoundingBoxMax() { return boundingBoxMax; }
    public double getAreaKmSquared() { return areaKmSquared; }

    @Override
    public String toString() {
        return String.format("DisasterZone[center=%s, area=%.1f km^2]",
                centroid, areaKmSquared);
    }
}
