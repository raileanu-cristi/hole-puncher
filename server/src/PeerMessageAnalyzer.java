import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This program demonstrates how to implement PeerMessageAnalyzer
 *
 * @author criss.tmd@gmail.com
 */
public class PeerMessageAnalyzer extends Thread {
    private static final  String REGISTER_MSG = "REGISTER";
    private final PeerRepository peerRepository;
    private final ISendPacketToPeer peerSender;

    final DatagramPacket packet;

    public PeerMessageAnalyzer(final ISendPacketToPeer peerSender, final DatagramPacket packet) {
        this.peerSender = peerSender;
        this.peerRepository = new PeerRepository();
        this.packet = packet;
    }

    @Override
    public void run() {
        processMessage(packet);
    }

    private void processMessage(final DatagramPacket packet) {
        System.out.println("[PeerMessageAnalyzer] processMessage");
        if (packet == null) {
            return;
        }
        final InetAddress clientAddress = packet.getAddress();
        final int clientPort = packet.getPort();
        final String message = new String(packet.getData(), StandardCharsets.UTF_8);
        final String[] words = PeerMessageHelper.getNonEmptyWords(message);
        final String firstWord = words.length > 0 ? words[0].trim() : null;
        if (firstWord == null) {
            System.out.println("[PeerMessageAnalyzer] Error: no words in the message!");
            return;
        }

        try {
            switch (firstWord) {
                case REGISTER_MSG -> {
                    peerRepository.register(clientAddress);
                    System.out.println("[PeerMessageAnalyzer] REGISTER peer registered with ip " + clientAddress.getHostAddress());
                    sendMessageToClient("ACK_REGISTER", clientAddress, clientPort);
                }
                case "CONNECT" -> {
                    System.out.println("[PeerMessageAnalyzer] CONNECT ");
                    if (words.length > 1) {
                        final String hostName = words[1].trim();
                        System.out.println("[PeerMessageAnalyzer] CONNECT to host "+hostName);
                        peerRepository.addConnectionRequest(clientAddress, InetAddress.getByName(hostName));
                    }
                }
                case "POLL" -> {
                    System.out.println("[PeerMessageAnalyzer] POLL");
                    final List<InetAddress> connectionRequests = peerRepository.getAndRemoveConnectionRequests(clientAddress);
                    final String responsePoll = "POLL_RESPONSE " + PeerMessageHelper.mapAddressesToString(connectionRequests);
                    sendMessageToClient(responsePoll, clientAddress, clientPort);
                }
                case "ACK_POLL" -> {
                    System.out.println("[PeerMessageAnalyzer] ACK_POLL ");
                    for (int i = 1; i < words.length; i++) {
                        sendMessageToClient("ACK" + " " + clientAddress.getHostAddress(), InetAddress.getByName(words[i]), clientPort);
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(final String message, final InetAddress address, final int port) {
        System.out.println("[PeerMessageAnalyzer] sent message \""+message + "\" to " + address.getHostAddress() + ":" + port);
        final DatagramPacket packetToClient = new DatagramPacket(message.getBytes(), message.length());
        packetToClient.setAddress(address);
        packetToClient.setPort(port);
        peerSender.sendPacketToPeer(packetToClient);
    }
}
