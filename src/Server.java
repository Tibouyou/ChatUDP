import java.net.*;
import java.util.ArrayList;
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
            System.out.println("Received packet from " + packet.getAddress() + ":" + packet.getPort());
            String message = new String(packet.getData(), 0, packet.getLength());
            if (message.startsWith("pseudo:")) {
                String pseudo = message.split(":")[1];
                clientsAdress.put(pseudo, packet.getAddress());
                clientsPort.put(pseudo, packet.getPort());
                System.out.println("Client " + pseudo + " connected");
                return;
            }
            System.out.println("Message : " + message);
            for (String pseudo : clientsAdress.keySet()) {
                if (clientsAdress.get(pseudo) != packet.getAddress()) {
                    DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
                    server.send(response);
                }
            }
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
