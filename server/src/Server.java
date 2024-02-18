import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server extends DatagramServerThread implements ISendPacketToPeer {
    final static int BUFFER_LEN = HolePuncher.BUFFER_LEN;

    public Server(final int port) throws SocketException {
        super(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                final byte[] buffer = new byte[BUFFER_LEN];
                final DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                System.out.println("\n[Server] message received! \""+new String(request.getData(), StandardCharsets.UTF_8)+"\"");
                final PeerMessageAnalyzer requestHandler = new PeerMessageAnalyzer(this, request);
                requestHandler.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void sendPacketToPeer(final DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
