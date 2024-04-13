package net.okocraft.servermonitor.config;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultBoolean;

public record Notifications(
        Setting currentStatus,
        Setting serverStarted,
        Setting serverStopped,
        Setting serverNotStarted,
        Setting firstPingFailure
) {
    public record Setting(@DefaultBoolean(true) boolean enabled, String message) {
    }
}
