import java.io.IOException;
import java.net.*;

public class Server extends DatagramServerThread implements ISendPacketToPeer {
    final static int BUFFER_LEN = HolePuncher.BUFFER_LEN;
    private final PeerMessageAnalyzer peerMessageAnalyzer;


    public Server(int port) throws SocketException {
        super(port);
        peerMessageAnalyzer = new PeerMessageAnalyzer(this);
        peerMessageAnalyzer.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[BUFFER_LEN];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                peerMessageAnalyzer.addMessage(request);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void sendPacketToPeer(DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
