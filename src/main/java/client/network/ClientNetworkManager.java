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
    private final String serverUrl = "http://localhost:8080";
    private final String wsUrl = "ws://localhost:8090";
    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public ClientNetworkManager() {
        connectToWebSocket();
    }

    public void postPreferences(Volunteer volunteer) {
        try {
            String json = gson.toJson(volunteer);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/preferences"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> System.out.println("Submitted preferences: " + res.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postOptimizeRequest() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/optimize"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> System.out.println("Triggered optimization: " + res.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Consumer<List<Assignment>> assignmentCallback;

    public void onAssignmentReceived(Consumer<List<Assignment>> callback) {
        this.assignmentCallback = callback;
    }

    private void connectToWebSocket() {
        try {
            WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to WebSocket server.");
                }

                @Override
                public void onMessage(String message) {
                    Assignment[] assignments = gson.fromJson(message, Assignment[].class);
                    if (assignmentCallback != null) {
                        assignmentCallback.accept(Arrays.asList(assignments));
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
