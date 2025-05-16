package server.core;

import model.Assignment;
import model.Preference;
import model.Volunteer;

import java.util.*;

/**
 * Uses a genetic algorithm to assign volunteers to services based on their preferences,
 * while respecting service capacity constraints and minimizing overall dissatisfaction.
 */
public class GeneticOptimizer {
    private final int maxGenerations = 100;      // Number of generations to evolve
    private final int populationSize = 50;       // Size of population in each generation
    private final double mutationRate = 0.1;     // Probability of mutation

    private final Map<String, Integer> serviceCapacities; // Max volunteers per service

    public GeneticOptimizer(Map<String, Integer> serviceCapacities) {
        this.serviceCapacities = serviceCapacities;
    }

    /**
     * Entry point for optimization process.
     */
    public List<Assignment> optimize(Collection<Volunteer> volunteers) {
        List<Map<Integer, String>> population = generateInitialPopulation(volunteers);

        for (int gen = 0; gen < maxGenerations; gen++) {
            // Sort by fitness (lower cost is better)
            population.sort(Comparator.comparingDouble(assignments -> computeTotalCost(assignments, volunteers)));

            List<Map<Integer, String>> nextGen = new ArrayList<>();

            // Elitism: retain top 10%
            int eliteCount = (int) (populationSize * 0.1);
            nextGen.addAll(population.subList(0, eliteCount));

            // Fill rest of next generation with offspring
            while (nextGen.size() < populationSize) {
                Map<Integer, String> parent1 = select(population, volunteers);
                Map<Integer, String> parent2 = select(population, volunteers);
                Map<Integer, String> child = crossover(parent1, parent2);
                mutate(child);
                nextGen.add(child);
            }

            population = nextGen;
        }

        // Return the best assignment after all generations
        Map<Integer, String> best = population.get(0);
        return toAssignmentList(best, volunteers);
    }

    /**
     * Generates initial population with random valid assignments.
     */
    private List<Map<Integer, String>> generateInitialPopulation(Collection<Volunteer> volunteers) {
        List<Map<Integer, String>> population = new ArrayList<>();
        List<String> services = new ArrayList<>(serviceCapacities.keySet());

        for (int i = 0; i < populationSize; i++) {
            Map<Integer, String> assignment = new HashMap<>();
            Map<String, Integer> serviceLoad = new HashMap<>();

            for (Volunteer v : volunteers) {
                List<String> prefs = new ArrayList<>();
                for (Preference p : v.getPreferences()) {
                    prefs.add(p.getServiceName());
                }
                Collections.shuffle(prefs); // Randomize preference order

                String chosen = null;
                for (String s : prefs) {
                    int used = serviceLoad.getOrDefault(s, 0);
                    if (used < serviceCapacities.getOrDefault(s, 5)) {
                        chosen = s;
                        serviceLoad.put(s, used + 1);
                        break;
                    }
                }

                // Fallback if all preferences are full
                if (chosen == null) {
                    for (String s : services) {
                        int used = serviceLoad.getOrDefault(s, 0);
                        if (used < serviceCapacities.get(s)) {
                            chosen = s;
                            serviceLoad.put(s, used + 1);
                            break;
                        }
                    }
                }

                assignment.put(v.getId(), chosen);
            }

            population.add(assignment);
        }

        return population;
    }

    /**
     * Computes the cost of a full assignment (lower is better).
     */
    private double computeTotalCost(Map<Integer, String> assignment, Collection<Volunteer> volunteers) {
        // Track how many are assigned to each service
        Map<String, Integer> count = new HashMap<>();
        for (String service : assignment.values()) {
            count.put(service, count.getOrDefault(service, 0) + 1);
        }

        // Check for over-capacity (hard constraint)
        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            if (entry.getValue() > serviceCapacities.getOrDefault(entry.getKey(), Integer.MAX_VALUE)) {
                return Double.MAX_VALUE;
            }
        }

        // Sum up dissatisfaction cost
        double total = 0;
        for (Volunteer v : volunteers) {
            String assigned = assignment.get(v.getId());
            total += calculateCost(v, assigned);
        }

        return total;
    }

    /**
     * Calculates dissatisfaction cost for a single volunteer assignment.
     */
    private double calculateCost(Volunteer v, String service) {
        List<Preference> prefs = v.getPreferences();
        for (int i = 0; i < prefs.size(); i++) {
            if (prefs.get(i).getServiceName().equals(service)) {
                return Math.pow(i, 2); // Quadratic penalty for lower-ranked preferences
            }
        }

        // Heavy penalty if the service isn't in the preference list
        int Nd = prefs.size();
        return 10 * Nd * Nd;
    }

    /**
     * Produces a new child assignment by combining two parents.
     */
    private Map<Integer, String> crossover(Map<Integer, String> p1, Map<Integer, String> p2) {
        Map<Integer, String> child = new HashMap<>();
        for (Integer id : p1.keySet()) {
            child.put(id, Math.random() < 0.5 ? p1.get(id) : p2.get(id));
        }
        return child;
    }

    /**
     * Randomly mutate a single assignment.
     */
    private void mutate(Map<Integer, String> assignment) {
        if (Math.random() > mutationRate) return;

        List<Integer> ids = new ArrayList<>(assignment.keySet());
        int index = new Random().nextInt(ids.size());
        int volId = ids.get(index);

        // Calculate current usage
        Map<String, Integer> currentLoad = new HashMap<>();
        for (String service : assignment.values()) {
            currentLoad.put(service, currentLoad.getOrDefault(service, 0) + 1);
        }

        // Find services that still have capacity
        List<String> candidates = new ArrayList<>();
        for (String service : serviceCapacities.keySet()) {
            if (currentLoad.getOrDefault(service, 0) < serviceCapacities.get(service)) {
                candidates.add(service);
            }
        }

        if (!candidates.isEmpty()) {
            String newService = candidates.get(new Random().nextInt(candidates.size()));
            assignment.put(volId, newService); // Mutate the assignment
        }
    }

    /**
     * Selects a parent from the population using tournament selection.
     */
    private Map<Integer, String> select(List<Map<Integer, String>> population, Collection<Volunteer> volunteers) {
        // Tournament: pick two and keep the better one
        int i = new Random().nextInt(population.size());
        int j = new Random().nextInt(population.size());

        // Create mapping of volunteer IDs to Volunteer objects for cost calculation
        Map<Integer, Volunteer> volunteerMap = new HashMap<>();
        for (Volunteer v : volunteers) {
            volunteerMap.put(v.getId(), v);
        }

        double costI = computeTotalCost(population.get(i), volunteerMap.values());
        double costJ = computeTotalCost(population.get(j), volunteerMap.values());

        return costI < costJ ? population.get(i) : population.get(j);
    }

    /**
     * Converts an internal assignment map to a list of Assignment objects.
     */
    private List<Assignment> toAssignmentList(Map<Integer, String> map, Collection<Volunteer> volunteers) {
        List<Assignment> result = new ArrayList<>();
        for (Volunteer v : volunteers) {
            String s = map.get(v.getId());
            double cost = calculateCost(v, s);
            result.add(new Assignment(v.getId(), s, cost));
        }
        return result;
    }
}
