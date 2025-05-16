package client.network;

import com.google.gson.Gson;
import model.Assignment;
import model.Volunteer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URISyntaxException;

public class ClientNetworkManager {
    // Base URL for HTTP communication with the server
    private final String serverUrl = "http://localhost:8080";

    // WebSocket URL for receiving real-time assignment updates
    private final String wsUrl = "ws://localhost:8090";

    // HTTP client used to send REST requests
    private final HttpClient http = HttpClient.newHttpClient();

    // Gson instance for converting Java objects to/from JSON
    private final Gson gson = new Gson();

    // Constructor initializes WebSocket connection
    public ClientNetworkManager() {
        connectToWebSocket();
    }

    // Sends the volunteer's preferences to the backend server using HTTP POST
    public void postPreferences(Volunteer volunteer) {
        try {
            String json = gson.toJson(volunteer); // Convert Volunteer object to JSON

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/preferences")) // Endpoint for submitting preferences
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)) // Send JSON in request body
                    .build();

            // Asynchronously send the request and print the response
            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> System.out.println("Submitted preferences: " + res.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sends a POST request to trigger the optimization algorithm on the server
    public void postOptimizeRequest() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/optimize")) // Endpoint to trigger optimization
                    .POST(HttpRequest.BodyPublishers.noBody()) // No body needed
                    .build();

            // Asynchronously send the request and print the response
            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> System.out.println("Triggered optimization: " + res.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A callback that gets called whenever a new list of assignments is received via WebSocket
    private Consumer<List<Assignment>> assignmentCallback;

    // Register a listener for assignment updates
    public void onAssignmentReceived(Consumer<List<Assignment>> callback) {
        this.assignmentCallback = callback;
    }

    // Establishes a WebSocket connection to receive real-time assignment updates
    private void connectToWebSocket() {
        try {
            WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {

                // Called when WebSocket connection is established
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to WebSocket server.");
                }

                // Called when a message (JSON-encoded list of assignments) is received
                @Override
                public void onMessage(String message) {
                    Assignment[] assignments = gson.fromJson(message, Assignment[].class); // Deserialize JSON
                    if (assignmentCallback != null) {
                        assignmentCallback.accept(Arrays.asList(assignments)); // Notify listener
                    }
                }

                // Called when the WebSocket connection is closed
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }

                // Called if an error occurs on the WebSocket
                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            client.connect(); // Initiates connection
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
