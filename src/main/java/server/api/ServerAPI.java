package server.api;

import com.google.gson.Gson;
import model.Assignment;
import model.Preference;
import model.Volunteer;
import server.broadcast.AssignmentBroadcaster;
import server.core.GeneticOptimizer;
import server.core.PreferenceStore;

import static spark.Spark.*;

import java.util.*;

/**
 * Defines REST API endpoints and optimization trigger for the volunteer assignment system.
 */
public class ServerAPI {
    private final PreferenceStore store = new PreferenceStore();             // Stores preferences from all volunteers
    private final AssignmentBroadcaster broadcaster = new AssignmentBroadcaster(); // Broadcasts assignment results
    private final Gson gson = new Gson();                                    // For JSON serialization/deserialization

    // Defines max capacities for each service
    private final Map<String, Integer> serviceCapacities = Map.of(
            "Reception", 4,
            "Logistics", 3,
            "Food Service", 5,
            "Security", 4,
            "Media", 2,
            "Transport", 3,
            "Medical", 2,
            "Info Desk", 3,
            "Cleanup", 4,
            "Tech Support", 3
    );

    /**
     * Initializes and starts the HTTP server with defined endpoints.
     */
    public void start() {
        port(8080); // Server listens on port 8080

        // Enables CORS for all origins (useful for frontend communication)
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
        });

        // POST endpoint to receive or update volunteer preferences
        post("/preferences", (req, res) -> {
            Volunteer v = gson.fromJson(req.body(), Volunteer.class); // Parse JSON to Volunteer
            store.addOrUpdatePreferences(v);                          // Save or update preferences
            return "Preferences received.";                           // Response message
        });

        // POST endpoint to trigger optimization and broadcast assignment results
        post("/optimize", (req, res) -> {
            new Thread(() -> { // Run optimization asynchronously
                System.out.println("Optimization thread started.");
                GeneticOptimizer optimizer = new GeneticOptimizer(serviceCapacities); // Initialize optimizer with capacities
                List<Assignment> result = optimizer.optimize(store.getAllVolunteers()); // Perform optimization
                System.out.println("Optimization thread finished. Broadcasting results.");
                broadcaster.broadcastAssignments(result); // Send results to all WebSocket clients
            }).start();
            return "Optimization started."; // Immediate response to client
        });
    }

    /**
     * Exposes the broadcaster (used for setting up WebSocket broadcasting).
     */
    public AssignmentBroadcaster getBroadcaster() {
        return broadcaster;
    }

    /**
     * Exposes the internal preference store (e.g., for testing or debugging).
     */
    public PreferenceStore getStore() {
        return store;
    }
}
