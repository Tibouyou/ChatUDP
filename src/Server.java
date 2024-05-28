import java.net.*;
import java.util.HashMap;

public class Server implements Runnable{
    DatagramSocket server;
    HashMap<String, InetAddress> clientsAdress = new HashMap<>();
    HashMap<String, Integer> clientsPort = new HashMap<>();

    public Server() {
        try {
            server = new DatagramSocket(25565);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            server.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            if (message.startsWith("pseudo:")) {
                String pseudo = message.split(":")[1];
                clientsAdress.put(pseudo, packet.getAddress());
                clientsPort.put(pseudo, packet.getPort());
                System.out.println("Client " + pseudo + " connected");
                String messageToSend = "Welcome on server Miguel (=^・ェ・^=) " + pseudo + " !";
                DatagramPacket response = new DatagramPacket(messageToSend.getBytes(), messageToSend.length(), packet.getAddress(), packet.getPort());
                server.send(response);
                return;
            }
            String pseudoSender = clientsAdress.entrySet().stream().filter(entry -> entry.getValue().equals(packet.getAddress())).findFirst().get().getKey();
            message = pseudoSender + " : " + message;
            System.out.println(message);
            for (String pseudo : clientsAdress.keySet()) {
                DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
                server.send(response);
            }
            System.out.println(clientsAdress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return server.getLocalPort();
    }

    @Override
    public void run() {
        while (true) {
            listen();
        }
    }
}
