import java.net.*;
import java.util.Scanner;
public class Client implements Runnable{
    DatagramSocket client;
    int port;

    public Client(int port) {
        try {
            client = new DatagramSocket();
            this.port = port;
        } catch (SocketException e) {
            e.printStackTrace();
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
