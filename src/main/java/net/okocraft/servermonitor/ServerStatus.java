package net.okocraft.servermonitor;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.okocraft.servermonitor.config.Notifications;
import org.jetbrains.annotations.NotNull;

public class ServerStatus {

    private final ServerInfo serverInfo;
    private final DiscordWebhookService webhookService;

    private Status status = Status.UNKNOWN;

    public ServerStatus(@NotNull DiscordWebhookService webhookService, @NotNull ServerInfo serverInfo) {
        this.webhookService = webhookService;
        this.serverInfo = serverInfo;
    }

    public void updateStatus(@NotNull RegisteredServer server) {
        server.ping().thenAcceptAsync(this::success).exceptionallyAsync(this::failure);
    }

    private void success(@NotNull ServerPing ignored) {
        switch (this.status) {
            case RUNNING, UNKNOWN -> this.webhookService.sendNotification(Notifications::currentStatus, this.serverInfo);
            case FIRST_FAILURE, STOPPED -> this.webhookService.sendNotification(Notifications::serverStarted, this.serverInfo);
        }
        this.status = Status.RUNNING;
    }

    private Void failure(@NotNull Throwable e) {
        switch (this.status) {
            case RUNNING -> {
                this.status = Status.FIRST_FAILURE;
                this.webhookService.sendNotification(Notifications::firstPingFailure, this.serverInfo);
            }
            case FIRST_FAILURE -> {
                this.status = Status.STOPPED;
                this.webhookService.sendNotification(Notifications::serverStopped, this.serverInfo);
            }
            case UNKNOWN -> {
                this.status = Status.STOPPED;
                this.webhookService.sendNotification(Notifications::serverNotStarted, this.serverInfo);
            }
        }
        return null;
    }

    private enum Status {
        RUNNING,
        FIRST_FAILURE,
        STOPPED,
        UNKNOWN
    }
}
