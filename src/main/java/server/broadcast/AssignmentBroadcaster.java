package server.broadcast;

import com.google.gson.Gson;
import model.Assignment;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A WebSocket server that manages client connections and broadcasts assignment results.
 */
public class AssignmentBroadcaster extends WebSocketServer {
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>()); // Active client connections
    private final Gson gson = new Gson(); // For converting assignment data to JSON

    /**
     * Constructor initializes the WebSocket server on port 8090.
     */
    public AssignmentBroadcaster() {
        super(new InetSocketAddress(8090)); // WebSocket will listen on port 8090
        start(); // Start the server
        System.out.println("WebSocket server started on port 8090");
    }

    /**
     * Triggered when a client connects.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn); // Add client to the set
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }

    /**
     * Triggered when a client disconnects.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn); // Remove client from the set
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    /**
     * Not used in this application because the server only sends messages.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // No need to handle incoming messages
    }

    /**
     * Handles errors that occur on the WebSocket server.
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Called when the WebSocket server is fully initialized.
     */
    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully");
    }

    /**
     * Broadcasts a list of assignment results to all connected clients.
     */
    public void broadcastAssignments(List<Assignment> assignments) {
        String json = gson.toJson(assignments); // Convert list to JSON string
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(json); // Send to each connected client
            }
        }
        System.out.println("Assignments broadcasted to all clients.");
    }
}
