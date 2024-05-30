import java.io.FileOutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainServer implements Runnable{
    DatagramSocket server;
    private final HashMap<String, InetAddress> clientsAddress = new HashMap<>();
    private final HashMap<String, Integer> clientsPort = new HashMap<>();

    private final HashMap<String, ArrayList<String>> rooms = new HashMap<>();
    private final HashMap<String, String> clientsRoom = new HashMap<>();

    String logFile;

    public MainServer() {
        try {
            server = new DatagramSocket(25565);
            logFile = "logs/log.txt";
            log("Server started\n");
            rooms.put("general", new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listenConnexion() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            server.receive(packet);
            int portSender = packet.getPort();
            InetAddress addressSender = packet.getAddress();
            String pseudo = new String(packet.getData(), 0, packet.getLength());
            if (clientsAddress.containsKey(pseudo)) {
                String message = "Pseudo already taken";
                DatagramPacket packet2 = new DatagramPacket(message.getBytes(), message.length(), addressSender, portSender);
                server.send(packet2);
                return;
            }
            clientsAddress.put(pseudo, packet.getAddress());
            clientsPort.put(pseudo, packet.getPort());
            clientsRoom.put(pseudo, "general");
            UserServer userServer = new UserServer(portSender, addressSender, pseudo, this);
            Thread userServerThread = new Thread(userServer);
            userServerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUserInRoom(String room, String pseudo) {
        rooms.get(room).add(pseudo);
    }


    public void createRoom(String roomName) {
        rooms.put(roomName, new ArrayList<>());
    }

    public void joinRoom(String roomName, String pseudo) {
        rooms.get(roomName).add(pseudo);
        clientsRoom.put(pseudo, roomName);
    }

    public void leaveRoom(String roomName, String pseudo) {
        rooms.get(roomName).remove(pseudo);
        if (rooms.get(roomName).isEmpty() && !roomName.equals("general")) {
            rooms.remove(roomName);
        }
        clientsRoom.put(pseudo, "general");
    }

    public HashMap<String, InetAddress> getClientsAddress() {
        return clientsAddress;
    }

    public HashMap<String, ArrayList<String>> getRooms() {
        return rooms;
    }

    public String getRoom(String pseudo) {
        return clientsRoom.get(pseudo);
    }

    public ArrayList<String> getUserInRoom(String room) {
        return rooms.get(room);
    }

    public InetAddress getAddress(String pseudo) {
        return clientsAddress.get(pseudo);
    }

    public int getPort(String pseudo) {
        return clientsPort.get(pseudo);
    }

    public void disconnect(String pseudo) {
        System.out.println(pseudo);
        clientsAddress.remove(pseudo);
        clientsPort.remove(pseudo);
        String room = clientsRoom.get(pseudo);
        clientsRoom.remove(pseudo);
        rooms.get(room).remove(pseudo);
        if (rooms.get(room).isEmpty() && !room.equals("general")) {
            rooms.remove(room);
        }
        System.out.println("Client " + pseudo + " disconnected\n");
        log("Client " + pseudo + " disconnected\n");
    }

    private void log(String message) {
        try {
            FileOutputStream fos = new FileOutputStream(logFile, true);
            String date = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss] ").format(Calendar.getInstance().getTime());
            fos.write((date + message.strip()+"\n").getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> log("Server stopped\n")));

        while (true) {
            listenConnexion();
        }
    }
}
