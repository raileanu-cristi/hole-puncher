import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DummyClient {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Syntax: DummyClient <host> <port> <targetHost> <id>");
            return;
        }
 
        final String hostname = args[0];
        final int clientPort = Integer.parseInt(args[1]);
        final int serverPort = Integer.parseInt(args[2]);
        final String targetHostname = args[3];
        final int id = Integer.parseInt(args[4]);

        try {
            final InetAddress address = InetAddress.getByName(hostname);
            final DatagramSocket socket = new DatagramSocket(clientPort);
            final InetAddress targetAddress = InetAddress.getByName(targetHostname);
            if (id == 1) {
                test1(socket, address, serverPort);
            } else if (id == 2) {
                test2(socket, address, serverPort, targetAddress);
            } else if (id == 3) {
                test3(socket, address, serverPort);
            }
        } catch (Exception ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } 
    }

    private static void test1(final DatagramSocket socket, final InetAddress address, final int port) throws IOException, InterruptedException, SecurityException  {
        final String dataStr = registerHost(socket, address, port);
        System.out.println(dataStr);
        TimeUnit.SECONDS.sleep(2);
        for (int i=0; i<2; i++) {
            final List<InetAddress> addresses = pollServer(socket, address, port);

            addresses.forEach(addr -> {
                try {
                    sendMessage("from host "+InetAddress.getLocalHost(), socket, addr, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendMessage("ACK_POLL "+mapAddressesToString(addresses), socket, address, port);
            System.out.println("ACK_POLL sent");
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static void test2(final DatagramSocket socket, final InetAddress address, final int port, final InetAddress targeAddress) throws IOException {
        sendMessage("CONNECT"+" "+targeAddress, socket, address, port);
        String response = receiveMessage(socket);
        System.out.println(response);
    }

    private static void test3(final DatagramSocket socket, final InetAddress address, final int port) throws IOException, InterruptedException {
        while (true) {
            sendMessage("CONNECT", socket, address, port);

            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static List<InetAddress> pollServer(final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        sendMessage("POLL", socket, address, port);
        final String pollResponse = receiveMessage(socket);
        System.out.println("POLL response= "+pollResponse);
        final String[] responseSplit = pollResponse.split(" ");
        final List<String> addresStr = Arrays.asList(responseSplit).subList(1, responseSplit.length);
        return addresStr
                .stream()
                .map(word -> {
                    try {
                        return InetAddress.getByName(word);
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).toList();
    }

    private static String registerHost(final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        sendMessage("REGISTER", socket, address, port);
        return receiveMessage(socket);
    }

    private static void sendMessage(final String message, final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        final byte[] buffer = message.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(request);
    }

    private static String receiveMessage(final DatagramSocket socket) throws IOException {
        final DatagramPacket response = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        socket.receive(response);
        return new String(response.getData(), 0, response.getLength());
    }

    public static String mapAddressesToString(final List<InetAddress> connectionRequests) {
        return connectionRequests.stream().map(inet -> inet.toString() + " ").reduce("", String::concat).trim();
    }
}
