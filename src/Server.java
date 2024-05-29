import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class Server implements Runnable{
    DatagramSocket server;
    HashMap<String, InetAddress> clientsAdress = new HashMap<>();
    HashMap<String, Integer> clientsPort = new HashMap<>();

    String logFile;

    public Server() {
        try {
            server = new DatagramSocket(25565);
            logFile = "logs/log.txt";
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(("Server started\n").getBytes());
            fos.close();
        } catch (Exception e) {
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
                newUser(packet);
                return;
            }
            if (message.startsWith("/")) {
                switch (message) {
                    case "/quit" -> {
                        disconnect(packet.getAddress());
                    }
                }
            }
            sendMessage(packet,message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect(InetAddress address) {
        String pseudo = clientsAdress.entrySet().stream().filter(entry -> entry.getValue().equals(address)).findFirst().get().getKey();
        clientsAdress.remove(pseudo);
        clientsPort.remove(pseudo);
        System.out.println("Client " + pseudo + " disconnected\n");
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(("Client " + pseudo + " disconnected\n").getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(DatagramPacket packet, String message) throws IOException {
        String pseudoSender = clientsAdress.entrySet().stream().filter(entry -> entry.getValue().equals(packet.getAddress())).findFirst().get().getKey();
        message = pseudoSender + " : " + message;
        System.out.println(message);
        FileOutputStream fos = new FileOutputStream(logFile, true);
        fos.write((clientsAdress.get(pseudoSender)+":"+clientsPort.get(pseudoSender)+"/"+message+"\n").getBytes());
        fos.close();
        for (String pseudo : clientsAdress.keySet()) {
            DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
            server.send(response);
        }
    }

    public void newUser(DatagramPacket packet) throws IOException {
        String message = new String(packet.getData(), 0, packet.getLength());
        String pseudo = message.split(":")[1];
        clientsAdress.put(pseudo, packet.getAddress());
        clientsPort.put(pseudo, packet.getPort());
        System.out.println("Client " + pseudo + " connected\n");
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(("Client " + pseudo + " connected\n").getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String messageToSend = "Welcome on server Miguel (=^・ェ・^=) !";
        DatagramPacket response = new DatagramPacket(messageToSend.getBytes(), messageToSend.length(), packet.getAddress(), packet.getPort());
        server.send(response);
    }

    public int getPort() {
        return server.getLocalPort();
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(logFile, true);
                    fos.write(("Server stopped\n").getBytes());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        while (true) {
            listen();
        }
    }
}
