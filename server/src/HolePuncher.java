import java.net.SocketException;

public class HolePuncher {
    final static int BUFFER_LEN = 8192;
    
    public static void main(String[] args) {
        if (args.length < 1) {
            Logger.error("Syntax: Server <port>");
            return;
        }
 
        int port = Integer.parseInt(args[0]);

        try {
            final Server server = new Server(port);
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
