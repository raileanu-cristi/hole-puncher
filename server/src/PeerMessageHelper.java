import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * This program demonstrates how to implement PeerMessageAnalyzer
 *
 * @author criss.tmd@gmail.com
 */
public class PeerMessageHelper {

    private PeerMessageHelper() {}

    public static String[] getNonEmptyWords(final String message) {
        return Arrays.stream(message.trim().split(" "))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .toArray(String[]::new);
    }

    public static String mapAddressesToString(final List<InetAddress> connectionRequests) {
        return connectionRequests.stream().map(inet -> inet.toString() + " ").reduce("", String::concat).trim();
    }
}
