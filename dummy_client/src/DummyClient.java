import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DummyClient {
    private static final int BUFFER_SIZE = 8192;
    private static int id;
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Syntax: DummyClient <host> <port> <targetHost> <id>");
            return;
        }
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String targetHostname = args[2];
        id  = Integer.parseInt(args[3]);

        try {
            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket(port);
            InetAddress targeAddress = InetAddress.getByName(targetHostname);
            if (id==1) {
                test1(socket, address, port);
            } else if (id==2) {
                test2(socket, address, port, targeAddress);
            }
        } catch (Exception ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } 
    }

    private static void test1(DatagramSocket socket,InetAddress address, int port) throws IOException, InterruptedException, UnknownHostException, SecurityException  {
        sendMessage("REGISTER", socket, address, port);
        String dataStr = receiveMessage(socket);
        TimeUnit.SECONDS.sleep(2);
        while (true) {
            sendMessage("POLL", socket, address, port);
            final String pollResponse = receiveMessage(socket);
            final List<String> addresStr = Arrays.asList(pollResponse.split(" ")).subList(1, 10);
            final List<InetAddress> addresses = addresStr.stream().map(word -> {
                try {
                    return InetAddress.getByName(word);
                } catch (UnknownHostException e) {
                    return null;
                }
            }).filter(addr -> addr!=null) .collect(Collectors.toList());

            addresses.forEach(addr -> {
                try {
                    sendMessage("from host "+InetAddress.getLocalHost(), socket, addr, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static void test2(DatagramSocket socket, InetAddress address, int port, InetAddress targeAddress) throws IOException {
        sendMessage("CONNECT"+" "+targeAddress, socket, address, port);
        String response = receiveMessage(socket);
        System.out.println(response);
    }

    private static void sendMessage(String message, DatagramSocket socket,InetAddress address, int port) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(request);
    }

    private static String receiveMessage(DatagramSocket socket) throws IOException {
        DatagramPacket response = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        socket.receive(response);
        return new String(response.getData(), 0, response.getLength());
    }
}
