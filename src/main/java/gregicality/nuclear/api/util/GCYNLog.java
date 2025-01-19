package gregicality.nuclear.api.util;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class GCYNLog {

    public static Logger logger;

    private GCYNLog() {
    }

    public static void init(@NotNull Logger modLogger) {
        logger = modLogger;
    }
}
