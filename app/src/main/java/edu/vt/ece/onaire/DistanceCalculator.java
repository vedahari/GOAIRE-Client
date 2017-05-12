package edu.vt.ece.onaire;

/**
 * Created by vedahari on 4/15/2017.
 */

public class DistanceCalculator {
    final static int R = 6371; // Radius of the earth
    /**
     * Returns the distance between two points on the Earth.
     * Direct translation from http://en.wikipedia.org/wiki/Haversine_formula
     * @param lat1 Latitude of the first point in degrees
     * @param lon1 Longitude of the first point in degrees
     * @param lat2 Latitude of the second point in degrees
     * @param lon2 Longitude of the second point in degrees
     * @return The distance between the two points in meters
     */
    public static Double computeDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;
        return distance*1000;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
