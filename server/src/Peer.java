import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class Peer {
    private PeerStatus peerStatus;
    private final List<InetAddress> connectionRequests;

    public Peer(PeerStatus peerStatus) {
        this.peerStatus = peerStatus;
        this.connectionRequests = new ArrayList<>();
    }

    public PeerStatus getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(final PeerStatus status) {
        this.peerStatus = status;
    }

    public List<InetAddress> getConnectionRequests() {
        return connectionRequests;
    }

    public void addConnectionRequest(final InetAddress address) {
        connectionRequests.add(address);
    }

    public void clearConnectionRequests() {
        connectionRequests.clear();
    }
}
