import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
public class Client implements Runnable{
    DatagramSocket client;
    int port;

    String pseudo;
    InetAddress address;

    public Client(int port) {
        try {
            client = new DatagramSocket();
            this.port = port;
            address = InetAddress.getByName("localhost");
            joinServer();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void joinServer () {
        try {
            Scanner myObj = new Scanner(System.in);
            System.out.println("Enter your pseudo : ");
            pseudo = myObj.nextLine();
            byte[] buffer = ("pseudo:"+pseudo).getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            client.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacket() {
        try {
            Scanner myObj = new Scanner(System.in);
            byte[] buffer = myObj.nextLine().getBytes();
            if (new String(buffer).equals("/quit")) {
                System.exit(0);
            }
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            client.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return client.getLocalPort();
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buffer = "/quit".getBytes();
                    InetAddress address = InetAddress.getByName("localhost");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    client.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        Thread sendPacket = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    sendPacket();
                }
            }
        });

        Thread listen = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    listen();
                }
            }
        });

        sendPacket.start();
        listen.start();
    }
}
