import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

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
            log("Server started\n","");
            rooms.put("general", new ArrayList<>());
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
                String[] args = message.substring(command.length()).split(" ");
                args = Arrays.copyOfRange(args, 1, args.length);
                switch (command) {
                    case "/quit" -> {
                        disconnect(packet.getAddress(), packet.getPort());
                    }
                    case "/msg" -> {
                        String pseudo = args[0];
                        String messageToSend = message.substring(command.length() + pseudo.length() + 2);
                        sendPrivateMessage(pseudo, packet, messageToSend);
                    }
                    case "/room" -> {
                        String argument = args[0];
                        System.out.println(argument);
                        switch (argument) {
                            case "create" -> {
                                String roomName = args[1];
                                if (rooms.containsKey(roomName)) {
                                    sendMessageToUser(packet, "Room already exist");
                                    return;
                                }
                                rooms.put(roomName, new ArrayList<>());
                                rooms.get(roomName).add(getPseudo(packet.getAddress(), packet.getPort()));
                                rooms.get("general").remove(getPseudo(packet.getAddress(), packet.getPort()));
                                clientsRoom.put(getPseudo(packet.getAddress(), packet.getPort()), roomName);
                                sendMessageToUser(packet, "Room created");
                            }
                            case "join" -> {
                                String roomName = args[1];
                                if (!rooms.containsKey(roomName)) {
                                    sendMessageToUser(packet, "Room doesn't exist");
                                    return;
                                }
                                rooms.get(roomName).add(getPseudo(packet.getAddress(), packet.getPort()));
                                rooms.get("general").remove(getPseudo(packet.getAddress(), packet.getPort()));
                                clientsRoom.put(getPseudo(packet.getAddress(), packet.getPort()), roomName);
                                sendMessageToUser(packet, "Room joined");
                            }
                            case "leave" -> {
                                String roomName = args[1];
                                rooms.get(roomName).remove(getPseudo(packet.getAddress(), packet.getPort()));
                                sendMessageToUser(packet, "Room left");
                                if (rooms.get(roomName).isEmpty()) {
                                    rooms.remove(roomName);
                                }
                            }
                        }
                    }
                }
            } else {
                sendMessage(packet, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(DatagramPacket packet, String message) throws IOException {
        String pseudoSender = getPseudo(packet.getAddress(), packet.getPort());
        String room = clientsRoom.get(pseudoSender);
        message = pseudoSender + " : " + message;
        System.out.println(message);
        log(clientsAdress.get(pseudoSender)+":"+clientsPort.get(pseudoSender)+"/"+message+"\n","["+room+"]");
        for (String pseudo : rooms.get(room)) {
            DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
            server.send(response);
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
        message = "(private) " + pseudoSender + " : " + message;
        System.out.println(message);
        log(clientsAdress.get(pseudoSender)+":"+clientsPort.get(pseudoSender)+"/"+message+"\n","(private)");
        DatagramPacket response1 = new DatagramPacket(message.getBytes(), message.length(), clientsAdress.get(pseudo), clientsPort.get(pseudo));
        DatagramPacket response2 = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
        server.send(response1);
        server.send(response2);
    }

    public void disconnect(InetAddress address, int port) {
        String pseudo = getPseudo(address, port);
        System.out.println(pseudo);
        clientsAdress.remove(pseudo);
        clientsPort.remove(pseudo);
        System.out.println("Client " + pseudo + " disconnected\n");
        log("Client " + pseudo + " disconnected\n","");
    }

    public void newUser(DatagramPacket packet) throws IOException {
        String message = new String(packet.getData(), 0, packet.getLength());
        String pseudo = message.split(":")[1];
        clientsAdress.put(pseudo, packet.getAddress());
        clientsPort.put(pseudo, packet.getPort());
        clientsRoom.put(pseudo, "general");
        rooms.get("general").add(pseudo);
        System.out.println("Client " + pseudo + " connected\n");
        log("Client " + pseudo + " connected\n","");
        String messageToSend = "Welcome on server Miguel (=^・ェ・^=) !";
        DatagramPacket response = new DatagramPacket(messageToSend.getBytes(), messageToSend.length()+6, packet.getAddress(), packet.getPort());
        server.send(response);
    }

    public int getPort() {
        return server.getLocalPort();
    }

    private void log(String message,String room) {
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            String date = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss] ").format(Calendar.getInstance().getTime());
            fos.write((date + room + message).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                log("Server stopped\n","");
            }
        }));

        while (true) {
            listen();
        }
    }
}
