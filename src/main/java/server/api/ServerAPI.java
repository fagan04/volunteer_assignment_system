package server.api;

import com.google.gson.Gson;
import model.Assignment;

import model.Volunteer;
import server.broadcast.AssignmentBroadcaster;
import server.core.GeneticOptimizer;
import server.core.PreferenceStore;

import static spark.Spark.*;

import java.util.*;

public class ServerAPI {
    private final PreferenceStore store = new PreferenceStore();
    private final AssignmentBroadcaster broadcaster = new AssignmentBroadcaster();
    private final Gson gson = new Gson();

    // Define service capacities (random or fixed)
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
            "Tech Support", 3);

    public void start()
    {
        port(8080);

        // CORS (optional)
        before((req, res) ->
        {
            res.header("Access-Control-Allow-Origin", "*");
        });

        // Receive or update volunteer preferences
        post("/preferences", (req, res) ->
        {
            Volunteer v = gson.fromJson(req.body(), Volunteer.class);
            store.addOrUpdatePreferences(v);
            return "Preferences received.";
        });

        // Trigger optimization and broadcast results
        post("/optimize", (req, res) ->
        {
            new Thread(() ->
            {
                System.out.println("Optimization thread started.");
                GeneticOptimizer optimizer = new GeneticOptimizer(serviceCapacities);
                List<Assignment> result = optimizer.optimize(store.getAllVolunteers());
                System.out.println("Optimization thread finished. Broadcasting results.");
                broadcaster.broadcastAssignments(result);
            }).start();
            return "Optimization started.";
        });

    }

    // Access to broadcaster (for WebSocket setup)
    public AssignmentBroadcaster getBroadcaster()
    {
        return broadcaster;
    }

    public PreferenceStore getStore()
    {
        return store;
    }
}
