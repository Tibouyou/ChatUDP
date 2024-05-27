

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            server();
        } else {
            client(Integer.parseInt(args[0]));
        }
    }

    public static void server() {
        Server server = new Server();
        System.out.println("Server started on port " + server.getPort());
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    public static void client(int port) {
        Client client = new Client(port);
        System.out.println("Client started on port " + client.getPort());
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}