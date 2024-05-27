

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        System.out.println("Server started on port " + server.getPort());
        Client client = new Client(server.getPort());
        System.out.println("Client started on port " + client.getPort());
        Thread serverThread = new Thread(server);
        serverThread.start();
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}