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
 
        String hostname = args[0];
        final int port = Integer.parseInt(args[1]);
        String targetHostname = args[2];
        final int id = Integer.parseInt(args[3]);

        try {
            final InetAddress address = InetAddress.getByName(hostname);
            final DatagramSocket socket = new DatagramSocket(port);
            final InetAddress targeAddress = InetAddress.getByName(targetHostname);
            if (id ==1) {
                test1(socket, address, port);
            } else if (id ==2) {
                test2(socket, address, port, targeAddress);
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
        while (true) {
            final List<InetAddress> addresses = pollServer(socket, address, port);

            addresses.forEach(addr -> {
                try {
                    sendMessage("from host "+InetAddress.getLocalHost(), socket, addr, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendMessage("ACK_POLL "+addresses.stream().map(InetAddress::toString).reduce("", String::concat), socket, address, port);
            System.out.println("ACK_POLL sent");
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static void test2(final DatagramSocket socket, final InetAddress address, final int port, final InetAddress targeAddress) throws IOException {
        sendMessage("CONNECT"+" "+targeAddress, socket, address, port);
        String response = receiveMessage(socket);
        System.out.println(response);
    }

    private static List<InetAddress> pollServer(final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        sendMessage("POLL", socket, address, port);
        final String pollResponse = receiveMessage(socket);
        final List<String> addresStr = Arrays.asList(pollResponse.split(" ")).subList(1, 10);
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
}
