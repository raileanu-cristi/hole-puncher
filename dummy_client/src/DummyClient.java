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
            final DatagramSocket socket = new DatagramSocket(serverPort);
            final InetAddress targetAddress = InetAddress.getByName(targetHostname);
            if (id == 1) {
                hostPeer(socket, address, serverPort, clientPort);
            } else if (id == 2) {
                joiningPeer(socket, address, serverPort, targetAddress, clientPort);
            } else if (id == 3) {
                bombardPeer(socket, address, clientPort, serverPort);
            } else if (id == 4) {
                respondPeer(socket);
            } else if (id == 5) {
                askPeer(socket, address, clientPort);
            }
        } catch (Exception ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } 
    }

    private static void hostPeer(final DatagramSocket socket, final InetAddress serverAddress, final int serverPort,
                                 final int clientPort) throws IOException, InterruptedException, SecurityException  {
        final String dataStr = registerHost(socket, serverAddress, serverPort);
        final DatagramSocket peerSocket = new DatagramSocket(clientPort);
        System.out.println(dataStr);
        for (int i=0; i<20; i++) {
            final List<InetAddress> addresses = pollServer(socket, serverAddress, serverPort);

            addresses.forEach(addr -> {
                try {
                    sendMessage("WALL_BREAK host "+InetAddress.getLocalHost().getHostAddress(), peerSocket, addr, clientPort);
                    System.out.println("WALL_BREAK host sent to " + addr.getHostAddress() + " port " + clientPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendMessage("ACK_POLL "+mapAddressesToString(addresses), socket, serverAddress, serverPort);
            System.out.println("ACK_POLL sent");
            TimeUnit.SECONDS.sleep(2);
            if (!addresses.isEmpty()) {
                break;
            }
        }
        final DatagramPacket msg = receiveDataGram(peerSocket);
        System.out.println(datagramMessage(msg));
        sendMessage("Hello joiner" + msg.getAddress().getHostAddress(), peerSocket, msg.getAddress(), clientPort);
    }

    private static void joiningPeer(final DatagramSocket socket, final InetAddress serverAddress, final int port, final InetAddress peerAddress,
                                    final int clientPort) throws IOException {
        System.out.println("CONNECT " + peerAddress.getHostAddress());
        sendMessage("CONNECT " + peerAddress.getHostAddress(), socket, serverAddress, port);
        final String response = receiveMessage(socket);
        System.out.println(response);
        final DatagramSocket peerSocket = new DatagramSocket(clientPort);
        sendMessage("WALL_PUNCH from "+ InetAddress.getLocalHost().getHostAddress(), peerSocket, peerAddress, clientPort);
        final String lastResponse = receiveMessage(socket);
        System.out.println(lastResponse);
    }

    private static void bombardPeer(final DatagramSocket socket, final InetAddress address, final int port, final int replicas) throws IOException, InterruptedException {
        for (int i=0; i<replicas; i++) {
            System.out.println("BOMBARD");
            sendMessage("BOMBARD", socket, address, port);

            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static void respondPeer(final DatagramSocket socket) throws IOException {
        while (true) {
            final DatagramPacket response = receiveDataGram(socket);
            System.out.println(datagramMessage(response));
            sendMessage("ACK from "+InetAddress.getLocalHost().getHostAddress(), socket, response.getAddress(), response.getPort());
        }
    }

    private static void askPeer(final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        while (true) {
            sendMessage("Hi from peer!", socket, address, port);
            final String response = receiveMessage(socket);
            System.out.println(response);
        }
    }

    private static List<InetAddress> pollServer(final DatagramSocket socket, final InetAddress address, final int port) throws IOException {
        sendMessage("POLL", socket, address, port);
        final String pollResponse = receiveMessage(socket);
        System.out.println("POLL response= "+pollResponse);
        final String[] responseSplit = pollResponse.split(" ");
        final List<String> addressStrings = Arrays.asList(responseSplit).subList(1, responseSplit.length);
        System.out.println(addressStrings);
        return addressStrings
                .stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
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
        return datagramMessage(response);
    }

    public static String mapAddressesToString(final List<InetAddress> connectionRequests) {
        return connectionRequests.stream().map(inet -> inet.getHostAddress() + " ").reduce("", String::concat).trim();
    }

    private static DatagramPacket receiveDataGram(final DatagramSocket socket) throws IOException {
        final DatagramPacket response = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        socket.receive(response);
        return response;
    }

    private static String datagramMessage(final DatagramPacket response) {
        return new String(response.getData(), 0, response.getLength());
    }
}
