import java.net.DatagramPacket;

public interface ISendPacketToPeer {

    void sendPacketToPeer(final DatagramPacket packet);
}
