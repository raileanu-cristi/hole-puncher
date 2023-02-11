
public class Logger {
    public enum Priority {
        INFO,
        WARN,
        ERROR
    }

    private static Priority priority = Priority.INFO;

    public static void setPriority(final String priority) {
        for (Priority prio : Priority.values())
            if (prio.toString().equals(priority)) {
                Logger.priority = prio;
                return;
            }
        Logger.error("[Logger] wrong logger priority value: "+priority);
    }

    public static void log(final Priority priority, final String message) {
        if (priority.ordinal() >= Logger.priority.ordinal())
            System.out.println(message);
    }

    public static void error(final String message) {
        System.out.println(message);
    }

    public static void info(final String message) {
        if (Logger.priority == Priority.INFO)
            System.out.println(message);
    }
}