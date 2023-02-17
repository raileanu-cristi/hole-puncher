import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.*;

public class PeerRepository {
    protected final Map<InetAddress, Peer> peerMap = new HashMap<>();

    /**
     * registers a new hosting client
     *
     * @param address addess of the hosting client
     */
    public void register(final InetAddress address) {
        peerMap.put(address, new Peer(PeerStatus.Idle));
    }

    public void remove(final InetAddress address) {
        peerMap.remove(address);
    }

    /**
     * Adds a new connection request to an existing hosting client
     *
     * @param sourceAddress address of the joining client
     * @param targetAddress address of the hosting client
     */
    public void addConnectionRequest(final InetAddress sourceAddress, final InetAddress targetAddress) {
        if (peerMap.containsKey(targetAddress)) {
            peerMap.get(targetAddress).addConnectionRequest(sourceAddress);
        }
    }

    public List<InetAddress> getAndRemoveConnectionRequests(final InetAddress address) {
        if (peerMap.containsKey(address)) {
            final List<InetAddress> addresses = new ArrayList<>(peerMap.get(address).getConnectionRequests());
            peerMap.get(address).clearConnectionRequests();
            return addresses;
        } else {
            return new ArrayList<>();
        }
    }

    public void removeConnectionRequests(final InetAddress address) {
        if (peerMap.containsKey(address)) {
            peerMap.get(address).clearConnectionRequests();
        }
    }

}
