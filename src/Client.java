import java.io.IOException;
import java.net.*;
import java.util.Scanner;
public class Client implements Runnable{
    DatagramSocket client;
    int port;

    String pseudo;

    public Client(int port) {
        try {
            client = new DatagramSocket();
            this.port = port;
            Scanner myObj = new Scanner(System.in);
            System.out.println("Enter your pseudo : ");
            pseudo = myObj.nextLine();
            byte[] buffer = ("pseudo:"+pseudo).getBytes();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            client.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPacket() {
        try {
            Scanner myObj = new Scanner(System.in);
            System.out.println("Enter a message : ");
            byte[] buffer = myObj.nextLine().getBytes();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            client.send(packet);
            System.out.println("Packet sent to " + packet.getAddress() + ":" + packet.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            System.out.println("Received packet from " + packet.getAddress() + ":" + packet.getPort());
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Message : " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return client.getLocalPort();
    }

    @Override
    public void run() {
        while (true) {
            sendPacket();
            listen();
        }
    }
}
