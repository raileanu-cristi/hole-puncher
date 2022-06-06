import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class Peer {
    private PeerStatus peerStatus;
    private List<InetAddress> connectionRequests;

    public Peer(PeerStatus peerStatus) {
        this.peerStatus = peerStatus;
        this.connectionRequests = new ArrayList<InetAddress>();
    }

    public PeerStatus getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(PeerStatus status) {
        this.peerStatus = status;
    }

    public List<InetAddress> getConnectionRequests() {
        return connectionRequests;
    }

    public void addConnectionRequest(InetAddress address) {
        connectionRequests.add(address);
    }

    public void clearConnectionRequests() {
        connectionRequests.clear();
    }
}
