package server.core;

import model.Assignment;
import model.Preference;
import model.Volunteer;

import java.util.*;

public class GeneticOptimizer {
    private final int maxGenerations = 100;
    private final int populationSize = 50;
    private final double mutationRate = 0.1;

    private final Map<String, Integer> serviceCapacities;

    public GeneticOptimizer(Map<String, Integer> serviceCapacities) {
        this.serviceCapacities = serviceCapacities;
    }

    public List<Assignment> optimize(Collection<Volunteer> volunteers) {
        List<Map<Integer, String>> population = generateInitialPopulation(volunteers);

        for (int gen = 0; gen < maxGenerations; gen++) {
            population.sort(Comparator.comparingDouble(assignments -> computeTotalCost(assignments, volunteers)));

            List<Map<Integer, String>> nextGen = new ArrayList<>();

            // Elitism: carry top 10% directly
            int eliteCount = (int) (populationSize * 0.1);
            nextGen.addAll(population.subList(0, eliteCount));

            // Crossover
            while (nextGen.size() < populationSize) {
                Map<Integer, String> parent1 = select(population, volunteers);
                Map<Integer, String> parent2 = select(population, volunteers);
                Map<Integer, String> child = crossover(parent1, parent2);
                mutate(child);
                nextGen.add(child);
            }

            population = nextGen;
        }

        // Best solution after all generations
        Map<Integer, String> best = population.get(0);
        return toAssignmentList(best, volunteers);
    }

    // Initial random valid solutions
    private List<Map<Integer, String>> generateInitialPopulation(Collection<Volunteer> volunteers) {
        List<Map<Integer, String>> population = new ArrayList<>();
        List<String> services = new ArrayList<>(serviceCapacities.keySet());

        for (int i = 0; i < populationSize; i++) {
            Map<Integer, String> assignment = new HashMap<>();
            Map<String, Integer> serviceLoad = new HashMap<>();
            for (Volunteer v : volunteers) {
                List<String> prefs = new ArrayList<>();
                for (Preference p : v.getPreferences()) prefs.add(p.getServiceName());
                Collections.shuffle(prefs); // random preference order

                String chosen = null;
                for (String s : prefs) {
                    int used = serviceLoad.getOrDefault(s, 0);
                    if (used < serviceCapacities.getOrDefault(s, 5)) {
                        chosen = s;
                        serviceLoad.put(s, used + 1);
                        break;
                    }
                }
                // fallback: random service
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

    private double computeTotalCost(Map<Integer, String> assignment, Collection<Volunteer> volunteers) {
        double total = 0;
        for (Volunteer v : volunteers) {
            String assigned = assignment.get(v.getId());
            total += calculateCost(v, assigned);
        }
        return total;
    }

    private double calculateCost(Volunteer v, String service) {
        List<Preference> prefs = v.getPreferences();
        for (int i = 0; i < prefs.size(); i++) {
            if (prefs.get(i).getServiceName().equals(service)) {
                return Math.pow(i, 2);
            }
        }
        int Nd = prefs.size(); // worst-case penalty
        return 10 * Nd * Nd;
    }

    private Map<Integer, String> crossover(Map<Integer, String> p1, Map<Integer, String> p2) {
        Map<Integer, String> child = new HashMap<>();
        for (Integer id : p1.keySet()) {
            child.put(id, Math.random() < 0.5 ? p1.get(id) : p2.get(id));
        }
        return child;
    }

    private void mutate(Map<Integer, String> assignment) {
        if (Math.random() > mutationRate) return;

        List<Integer> keys = new ArrayList<>(assignment.keySet());
        int index = new Random().nextInt(keys.size());
        int volId = keys.get(index);
        List<String> serviceList = new ArrayList<>(serviceCapacities.keySet());
        String newService = serviceList.get(new Random().nextInt(serviceList.size()));
        assignment.put(volId, newService);
    }

    private Map<Integer, String> select(List<Map<Integer, String>> population, Collection<Volunteer> volunteers) {
        // Tournament selection
        int i = new Random().nextInt(population.size());
        int j = new Random().nextInt(population.size());
        double costI = computeTotalCost(population.get(i), population.get(0).keySet().stream().map(id -> volunteers.stream().filter(volunteer -> id == volunteer.getId()).findFirst().get()).toList()); 
        double costJ = computeTotalCost(population.get(i), population.get(0).keySet().stream().map(id -> volunteers.stream().filter(volunteer -> id == volunteer.getId()).findFirst().get()).toList()); 
        return costI < costJ ? population.get(i) : population.get(j);
    }

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
