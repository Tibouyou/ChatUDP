import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Server implements Runnable{
    DatagramSocket server;
    HashMap<String, InetAddress> clientsAdress = new HashMap<>();
    HashMap<String, Integer> clientsPort = new HashMap<>();

    HashMap<String, ArrayList<String>> rooms = new HashMap<>();
    HashMap<String, String> clientsRoom = new HashMap<>();

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
                String command = message.split(" ")[0];
                String[] args = message.substring(command.length()+1).split(" ");
                switch (command) {
                    case "/quit" -> {
                        disconnect(packet.getAddress());
                    }
                    case "/msg" -> {
                        String pseudo = args[0];
                        String messageToSend = message.substring(command.length() + pseudo.length() + 2);
                        sendPrivateMessage(pseudo, packet, messageToSend);
                    }
                    case "/room" -> {
                        String argument = args[0];
                        switch (argument) {
                            case "create" -> {
                                String roomName = args[1];
                                rooms.put(roomName, new ArrayList<>());
                                rooms.get(roomName).add(getPseudo(packet.getAddress(), packet.getPort()));
                                sendMessageToUser(packet, "Room created");
                            }
                            case "join" -> {
                                String roomName = args[1];
                                if (!rooms.containsKey(roomName)) {
                                    sendMessageToUser(packet, "Room doesn't exist");
                                    return;
                                }
                                rooms.get(roomName).add(getPseudo(packet.getAddress(), packet.getPort()));
                                sendMessageToUser(packet, "Room joined");
                            }
                            case "leave" -> {
                                String roomName = args[1];
                                rooms.get(roomName).remove(getPseudo(packet.getAddress(), packet.getPort()));
                                sendMessageToUser(packet, "Room left");
                            }
                        }
                    }
                }
            }else {
                sendMessage(packet, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToUser(DatagramPacket packet, String message) throws IOException {
        DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
        server.send(response);
    }

    public String getPseudo(InetAddress address, int port) {
        String[] pseudoWithAdress = clientsAdress.entrySet().stream().filter(entry -> entry.getValue().equals(address)).map(entry -> entry.getKey()).toArray(String[]::new);
        String[] pseudoWithPort = clientsPort.entrySet().stream().filter(entry -> entry.getValue().equals(port)).map(entry -> entry.getKey()).toArray(String[]::new);
        String pseudoSender = "";
        for (String pseudoAdress : pseudoWithAdress) {
            for (String pseudoPort : pseudoWithPort) {
                if (pseudoAdress.equals(pseudoPort)) {
                    pseudoSender = pseudoAdress;
                }
            }
        }
        return pseudoSender;
    }

    public void sendPrivateMessage(String pseudo, DatagramPacket packet, String message) throws IOException {
        String pseudoSender = getPseudo(packet.getAddress(), packet.getPort());
        message = "(private)" + pseudoSender + " : " + message;
        System.out.println(message);
        FileOutputStream fos = new FileOutputStream(logFile, true);
        fos.write((clientsAdress.get(pseudoSender)+":"+clientsPort.get(pseudoSender)+"/"+message+"\n").getBytes());
        fos.close();
        DatagramPacket response1 = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
        DatagramPacket response2 = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
        server.send(response1);
        server.send(response2);
    }

    public void disconnect(InetAddress address) {

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
