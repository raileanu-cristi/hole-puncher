import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Server extends DatagramServerThread implements ISendPacketToPeer {
    final static int BUFFER_LEN = HolePuncher.BUFFER_LEN;
    private final PeerMessageAnalyzer peerMessageAnalyzer;


    public Server(final int port) throws SocketException {
        super(port);
        peerMessageAnalyzer = new PeerMessageAnalyzer(this);
        peerMessageAnalyzer.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                final byte[] buffer = new byte[BUFFER_LEN];
                final DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                System.out.println("[Server] message received!");
                peerMessageAnalyzer.addMessage(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendPacketToPeer(final DatagramPacket packet) {
        try {
            socket.send(packet);
            System.out.println("[Server] message sent "+ Arrays.toString(packet.getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
