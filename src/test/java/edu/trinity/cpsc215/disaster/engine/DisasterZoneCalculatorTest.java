package edu.trinity.cpsc215.disaster.engine;

import edu.trinity.cpsc215.disaster.model.DisasterZone;
import edu.trinity.cpsc215.disaster.model.GpsCoordinate;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisasterZoneCalculatorTest {

    private DisasterZoneCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new DisasterZoneCalculator();
    }

    @Test
    @Order(1)
    void testEmptyCalculator() {
        assertNull(calc.getCentroid());
        assertNull(calc.calculate());
        assertEquals(0, calc.getCount());
    }

    @Test
    @Order(2)
    void testSinglePoint() {
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));

        GpsCoordinate centroid = calc.getCentroid();
        assertEquals(41.0, centroid.getLatitude(), 0.0001);
        assertEquals(29.0, centroid.getLongitude(), 0.0001);
    }

    @Test
    @Order(3)
    void testCentroidOfTwoPoints() {
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));
        calc.addCoordinate(new GpsCoordinate(41.2, 29.2));

        GpsCoordinate centroid = calc.getCentroid();
        assertEquals(41.1, centroid.getLatitude(), 0.0001);
        assertEquals(29.1, centroid.getLongitude(), 0.0001);
    }

    @Test
    @Order(4)
    void testBoundingBox() {
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));
        calc.addCoordinate(new GpsCoordinate(41.2, 29.3));
        calc.addCoordinate(new GpsCoordinate(40.8, 28.8));

        GpsCoordinate min = calc.getBoundingBoxMin();
        GpsCoordinate max = calc.getBoundingBoxMax();

        assertEquals(40.8, min.getLatitude(), 0.0001);
        assertEquals(28.8, min.getLongitude(), 0.0001);
        assertEquals(41.2, max.getLatitude(), 0.0001);
        assertEquals(29.3, max.getLongitude(), 0.0001);
    }

    @Test
    @Order(5)
    void testAreaCalculation() {
        // Box roughly 0.2 degrees lat x 0.2 degrees lon near Istanbul
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));
        calc.addCoordinate(new GpsCoordinate(41.2, 29.2));

        double area = calc.getAreaKmSquared();
        // ~22km x ~17km = ~374 km^2 (approximate)
        assertTrue(area > 200, "Area should be > 200 km^2, got " + area);
        assertTrue(area < 600, "Area should be < 600 km^2, got " + area);
    }

    @Test
    @Order(6)
    void testFullCalculation() {
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));
        calc.addCoordinate(new GpsCoordinate(41.1, 29.1));

        DisasterZone zone = calc.calculate();
        assertNotNull(zone);
        assertNotNull(zone.getCentroid());
        assertNotNull(zone.getBoundingBoxMin());
        assertNotNull(zone.getBoundingBoxMax());
        assertTrue(zone.getAreaKmSquared() > 0);
    }

    @Test
    @Order(7)
    void testIncrementalUpdate() {
        calc.addCoordinate(new GpsCoordinate(41.0, 29.0));
        GpsCoordinate c1 = calc.getCentroid();

        calc.addCoordinate(new GpsCoordinate(41.2, 29.2));
        GpsCoordinate c2 = calc.getCentroid();

        // Centroid should have shifted
        assertNotEquals(c1.getLatitude(), c2.getLatitude());
        assertEquals(2, calc.getCount());
    }
}
