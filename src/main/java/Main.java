import server.api.ServerAPI;

public class Main {
    public static void main(String[] args) {
        ServerAPI api = new ServerAPI();
        api.start(); // Start REST server and WebSocket broadcaster
    }
}
