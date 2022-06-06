import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class DatagramServerThread extends Thread {
    protected DatagramSocket socket;
    final int port;

    public DatagramServerThread(int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(port);
    }

    public int getPort() {
        return port;
    }
}
