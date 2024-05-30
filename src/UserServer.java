import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class UserServer implements Runnable{
    MainServer mainServer;
    DatagramSocket server;
    String pseudo;

    InetAddress address;
    int port;

    String logFile;

    public UserServer(int port, InetAddress address, String pseudo, MainServer mainServer) {
        try {
            server = new DatagramSocket();
            this.logFile = "logs/log.txt";
            this.port = port;
            this.address = address;
            this.pseudo = pseudo;
            this.mainServer = mainServer;
            log("Client "+pseudo+" is connected on port"+server.getLocalPort()+"\n");
            String message = "port:"+server.getLocalPort();
            sendMessageToUser(new DatagramPacket(message.getBytes(), message.length(), address, port),message);
            mainServer.addUserInRoom("general",pseudo);
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
            if (message.startsWith("/")) {
                String command = message.split(" ")[0];
                String[] args = message.substring(command.length()).split(" ");
                args = Arrays.copyOfRange(args, 1, args.length);
                switch (command) {
                    case "/quit" -> {
                        mainServer.disconnect(this.pseudo);
                        server.close();
                    }
                    case "/msg" -> {
                        String pseudo = args[0];
                        String messageToSend = message.substring(command.length() + pseudo.length() + 2);
                        sendPrivateMessage(pseudo, packet, messageToSend);
                    }
                    case "/list" -> {
                        String messageToSend = "Connected people : " + mainServer.getClientsAdress().keySet() + "\n" +
                                "Rooms : " + mainServer.getRooms().keySet();
                        sendMessageToUser(packet, messageToSend);
                    }
                    case "/miguel" -> {
                        String messageToSend = Files.readString(Path.of("data/miguel.txt"), StandardCharsets.UTF_8);
                        for (int i = 0; i < messageToSend.length(); i += 1024) {
                            String msg = messageToSend.substring(i, Math.min(messageToSend.length(), i + 1024));
                            DatagramPacket response = new DatagramPacket(msg.getBytes(), msg.getBytes().length, packet.getAddress(), packet.getPort());
                            server.send(response);
                        }
                    }
                    case "/help" -> {
                        String messageToSend = "Commands available :\n" +
                                "/quit : disconnect from the server\n" +
                                "/list : list connected people and rooms\n" +
                                "/msg [pseudo] [message] : send a private message to a user\n" +
                                "/room create [roomName] : create a room\n" +
                                "/room join [roomName] : join a room\n" +
                                "/room leave [roomName] : leave a room\n" +
                                "/miguel : summon Miguel\n";
                        sendMessageToUser(packet, messageToSend);
                    }
                    case "/room" -> {
                        if (args.length < 1) {
                            sendMessageToUser(packet, "Not enough arguments");
                            return;
                        }
                        if (args.length < 2 && !args[0].equals("leave")) {
                            sendMessageToUser(packet, "Not enough arguments");
                            return;
                        }
                        String argument = args[0];
                        switch (argument) {
                            case "create" -> {
                                String roomName = args[1];
                                if (mainServer.getRooms().containsKey(roomName)) {
                                    sendMessageToUser(packet, "Room already exist");
                                    return;
                                }
                                mainServer.createRoom(roomName);
                                mainServer.leaveRoom(mainServer.getRoom(pseudo), pseudo);
                                mainServer.joinRoom(roomName, pseudo);
                                sendMessageToUser(packet, "Room created");
                            }
                            case "join" -> {
                                String roomName = args[1];
                                if (!mainServer.getRooms().containsKey(roomName)) {
                                    sendMessageToUser(packet, "Room doesn't exist");
                                    return;
                                }
                                mainServer.leaveRoom(mainServer.getRoom(pseudo), pseudo);
                                mainServer.joinRoom(roomName, pseudo);
                                sendMessageToUser(packet, "Room joined");
                            }
                            case "leave" -> {
                                mainServer.leaveRoom(mainServer.getRoom(pseudo), pseudo);
                                sendMessageToUser(packet, "Room left");
                            }
                            default -> {
                                sendMessageToUser(packet, "Unknown command");
                            }
                        }
                    }
                    default -> {
                        sendMessageToUser(packet, "Unknown command");
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
        String room = mainServer.getRoom(pseudo);
        message = "["+mainServer.getRoom(this.pseudo)+"]" + this.pseudo + " : " + message + "\n";
        System.out.println(message);
        log(this.address+":"+this.port+"/"+message+"\n");
        for (String pseudo : mainServer.getUserInRoom(room)) {
            DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), mainServer.getAdress(pseudo), mainServer.getPort(pseudo));
            server.send(response);
        }
    }

    private void sendMessageToUser(DatagramPacket packet, String message) throws IOException {
        message += "\n";
        DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
        server.send(response);
    }

    public void sendPrivateMessage(String pseudo, DatagramPacket packet, String message) throws IOException {
        message = "(private) " + this.pseudo + " : " + message;
        System.out.println(message);
        log(this.address+":"+this.port+"/"+message+"\n");
        if (!mainServer.getClientsAdress().containsKey(pseudo)) {
            sendMessageToUser(packet, "User not found");
            return;
        }
        DatagramPacket response1 = new DatagramPacket(message.getBytes(), message.length(), mainServer.getAdress(pseudo), mainServer.getPort(pseudo));
        DatagramPacket response2 = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
        server.send(response1);
        server.send(response2);
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
        while (!server.isClosed()) {
            listen();
        }
    }
}
