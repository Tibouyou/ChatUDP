import java.net.*;
public class Server implements Runnable{
    DatagramSocket server;

    public Server() {
        try {
            server = new DatagramSocket();
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
            System.out.println("Message : " + message);
            DatagramPacket response = new DatagramPacket(message.getBytes(), message.length(), packet.getAddress(), packet.getPort());
            server.send(response);
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
