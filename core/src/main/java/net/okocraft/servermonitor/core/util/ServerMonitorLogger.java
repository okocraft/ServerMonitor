package net.okocraft.servermonitor.core.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;

public final class ServerMonitorLogger {

    private static final SubstituteLogger LOGGER = new SubstituteLogger("ServerMonitor", null, true);
    private static final SubstituteLogger DEBUG_LOGGER = new SubstituteLogger("ServerMonitor-Debug", null, true);

    static {
        try {
            Class.forName("org.junit.jupiter.api.Assertions");
            LOGGER.setDelegate(LoggerFactory.getLogger(ServerMonitorLogger.class));
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Gets ServerMonitor's {@link Logger}.
     *
     * @return ServerMonitor's {@link Logger}.
     */
    public static @NotNull Logger logger() {
        return LOGGER;
    }

    /**
     * Gets ServerMonitor's {@link Logger} for debugging.
     *
     * @return ServerMonitor's {@link Logger} for debugging.
     */
    public static @NotNull Logger debug() {
        return DEBUG_LOGGER;
    }

    /**
     * Enables or disables logging debugs.
     *
     * @param enabled new state
     */
    public static void logDebug(boolean enabled) {
        DEBUG_LOGGER.setDelegate(enabled ? LOGGER : null);
    }

    private ServerMonitorLogger() {
        throw new UnsupportedOperationException();
    }
}
