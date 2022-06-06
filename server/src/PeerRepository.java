import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.*;

public class PeerRepository {
    protected Map<InetAddress, Peer> peerMap = new HashMap<InetAddress, Peer>();

    public void register(InetAddress address) {
        peerMap.put(address, new Peer(PeerStatus.Idle));
    }

    public void remove(InetAddress address) {
        peerMap.remove(address);
    }

    public void addConnectionRequest(InetAddress sourceAddress, InetAddress targetAddress) {
        if (peerMap.containsKey(targetAddress)) {
            peerMap.get(targetAddress).addConnectionRequest(sourceAddress);
        }
    }

    public List<InetAddress> getConnectionRequests(InetAddress address) {
        if (peerMap.containsKey(address)) {
            return peerMap.get(address).getConnectionRequests();
        } else {
            return new ArrayList<>();
        }
    }

    public void removeConnectionRequests(InetAddress address) {
        if (peerMap.containsKey(address)) {
            peerMap.get(address).clearConnectionRequests();
        }
    }

}
