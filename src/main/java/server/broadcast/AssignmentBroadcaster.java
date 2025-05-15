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

public class AssignmentBroadcaster extends WebSocketServer {
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
    private final Gson gson = new Gson();

    public AssignmentBroadcaster() {
        super(new InetSocketAddress(8090)); // WebSocket port
        start(); // start listening
        System.out.println("WebSocket server started on port 8090");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Not needed for this project (clients only receive data)
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully");
    }

    public void broadcastAssignments(List<Assignment> assignments) {
        String json = gson.toJson(assignments);
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(json);
            }
        }
        System.out.println("Assignments broadcasted to all clients.");
    }
}
