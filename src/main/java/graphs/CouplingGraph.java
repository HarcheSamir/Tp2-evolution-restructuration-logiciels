
package graphs;

import graphs.distanceStrategy.NavigatorDistance;
import graphs.coupling.distanceStrategy.strategy.impls.AddStrategy;

import java.util.*;
import java.util.function.Consumer;

public class CouplingGraph {

    private Map<String, Map<String, Double>> couplingClasses  = new HashMap<>();
    private static NavigatorDistance navigatorDistance = new NavigatorDistance();
    private Set<String> names = new HashSet<>();
    private double[][] distances ;
    private Set<String> classes = new HashSet<>();

    public void setClasses(Set<String> classes) { this.classes = classes; }
    public Set<String> getClasses() { return classes; }
    public void setCouplingClasses(Map<String, Map<String, Double>> map) { this.couplingClasses = map; }
    public Map<String, Map<String, Double>> getCouplingGraph() { return this.couplingClasses; }
    public void addNodeToCouplingGraph(String source, Map<String, Double> map) {
        this.couplingClasses.put(source, map);
    }

    public String printCouplingGraph() {
        StringBuilder builder = new StringBuilder();
        builder.append("Coupling Graph\n");

        for (String source: couplingClasses.keySet() ) {
            builder.append(source).append(":\n");

            for (String destination: couplingClasses.get(source).keySet())
                builder.append("\t---> ").append(destination).append(" (").append(couplingClasses.get(source).get(destination)).append(")\n");
            builder.append("\n");
        }
        return builder.toString();
    }

    // --- START OF FIXES ---

    public double calculateDistance(String source, String destination) {
        double expectedDistance = 0.0;
        try {
            double distance1 = getFirstValueOfCoupling(source, destination);
            double distance2 = getSecondValueOfCoupling(destination, source);
            expectedDistance = 1 - calculateDistanceVariant(distance1, distance2);
        } catch (Exception e) {
            // This catch block is for safety, but our fixes should prevent exceptions.
            e.printStackTrace();
        }
        // Ensure distance is never negative
        return Math.max(0, expectedDistance);
    }

    protected double getFirstValueOfCoupling(String source, String destination) {
        // SAFE VERSION: Check if the source class exists in the map.
        if (couplingClasses.containsKey(source)) {
            Map<String, Double> destinationMap = couplingClasses.get(source);
            // SAFE VERSION: Check if the destination class is in the inner map and return the value or 0.
            return destinationMap.getOrDefault(destination, 0.0);
        }
        // If the source class made no calls, its coupling to anything is 0.
        return 0.0;
    }

    protected double getSecondValueOfCoupling(String destination, String source) {
        // This method can now just reuse the safe version of the first one.
        return getFirstValueOfCoupling(destination, source);
    }

    // --- END OF FIXES ---

    protected double calculateDistanceVariant(double distance1, double distance2) {
        navigatorDistance.setDistanceStrategy(new AddStrategy());
        return navigatorDistance.calculateDistance(distance1, distance2);
    }
}