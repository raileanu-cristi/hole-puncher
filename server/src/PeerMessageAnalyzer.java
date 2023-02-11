import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This program demonstrates how to implement PeerMessageAnalyzer
 *
 * @author criss.tmd@gmail.com
 */
public class PeerMessageAnalyzer extends Thread {
    private final String TEST_PEER_MSG = "TEST_PEER";
    private final Queue<DatagramPacket> msgQueue;
    private final PeerRepository peerRepository;
    private boolean isRunning;
    private final ISendPacketToPeer peerSender;

    public PeerMessageAnalyzer(ISendPacketToPeer peerSender) {
        isRunning = true;
        this.peerSender = peerSender;
        this.msgQueue = new ConcurrentLinkedQueue<>();
        this.peerRepository = new PeerRepository();
    }

    public void run() {
        while (isRunning) {
            if (!msgQueue.isEmpty()) {
                processMessage(msgQueue.poll());
            }
        }
    }

    public void stopRunning() {
        isRunning = false;
    }

    public void addMessage(final DatagramPacket message) {
        msgQueue.add(message);
    }

    private void processMessage(final DatagramPacket packet) {
        System.out.println("[PeerMessageAnalyzer] processMessage");
        if (packet == null) {
            return;
        }
        final InetAddress clientAddress = packet.getAddress();
        final int clientPort = packet.getPort();
        final String message = new String(packet.getData(), StandardCharsets.UTF_8);
        final String[] words = message.split(" ");
        final String firstWord = words.length > 0 ? words[0] : null;
        if (firstWord != null) {
            System.out.println("[PeerMessageAnalyzer] firstWord: "+firstWord);
        } else {
            System.out.println("[PeerMessageAnalyzer] Error: no words in the message!");
            return;
        }

        try {
            switch (firstWord) {
                case "REGISTER" -> {
                    peerRepository.register(clientAddress);
                    System.out.println("[PeerMessageAnalyzer] peer registered with ip " + clientAddress.toString());
                    sendMessageToClient("ACK_REGISTER", clientAddress, clientPort);
                }
                case "CONNECT" -> {
                    if (words.length > 1) {
                        peerRepository.addConnectionRequest(clientAddress, InetAddress.getByName(words[1]));
                    }
                }
                case "POLL" -> {
                    System.out.println("[PeerMessageAnalyzer] POLL");
                    final List<InetAddress> connectionRequests = peerRepository.getConnectionRequests(clientAddress);
                    peerRepository.removeConnectionRequests(clientAddress);
                    sendMessageToClient(connectionRequests.stream().map(InetAddress::toString).reduce("", String::concat), clientAddress, clientPort);
                }
                case "ACK_POLL" -> {
                    for (int i = 1; i < words.length; i++) {
                        sendMessageToClient("ACK" + " " + clientAddress.toString(), InetAddress.getByName(words[i]), clientPort);
                    }
                }
                default -> {
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(final String message, final InetAddress address, final int port) {
        final DatagramPacket packetToClient = new DatagramPacket(message.getBytes(), message.length());
        packetToClient.setAddress(address);
        packetToClient.setPort(port);
        peerSender.sendPacketToPeer(packetToClient);
    }
}
