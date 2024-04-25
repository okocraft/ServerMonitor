package net.okocraft.servermonitor.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.okocraft.servermonitor.core.config.ConfigHolder;
import net.okocraft.servermonitor.core.discord.DiscordWebhookService;
import net.okocraft.servermonitor.velocity.config.Config;
import net.okocraft.servermonitor.velocity.config.Notifications;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ServerStatusChecker implements Runnable {

    private final ProxyServer proxy;
    private final ConfigHolder<Config> configHolder;
    private final DiscordWebhookService webhookService;
    private final Map<ServerInfo, ServerStatus> statusMap = new HashMap<>();

    public ServerStatusChecker(@NotNull ProxyServer proxy, @NotNull ConfigHolder<Config> configHolder, @NotNull DiscordWebhookService webhookService) {
        this.proxy = proxy;
        this.configHolder = configHolder;
        this.webhookService = webhookService;
    }

    @Override
    public void run() {
        for (var server : this.proxy.getAllServers()) {
            var status = this.statusMap.computeIfAbsent(server.getServerInfo(), ServerStatus::new);
            status.updateStatus(server);
        }
    }

    private class ServerStatus {

        private final ServerInfo serverInfo;
        private Status status = Status.UNKNOWN;

        private ServerStatus(@NotNull ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        private void updateStatus(@NotNull RegisteredServer server) {
            server.ping().thenAcceptAsync(this::success).exceptionallyAsync(this::failure);
        }

        private void success(@NotNull ServerPing ignored) {
            switch (this.status) {
                case RUNNING, UNKNOWN -> this.sendNotification(Notifications::currentStatus, this.serverInfo);
                case FIRST_FAILURE, STOPPED -> this.sendNotification(Notifications::serverStarted, this.serverInfo);
            }
            this.status = Status.RUNNING;
        }

        private Void failure(@NotNull Throwable e) {
            switch (this.status) {
                case RUNNING -> {
                    this.status = Status.FIRST_FAILURE;
                    this.sendNotification(Notifications::firstPingFailure, this.serverInfo);
                }
                case FIRST_FAILURE -> {
                    this.status = Status.STOPPED;
                    this.sendNotification(Notifications::serverStopped, this.serverInfo);
                }
                case UNKNOWN -> {
                    this.status = Status.STOPPED;
                    this.sendNotification(Notifications::serverNotStarted, this.serverInfo);
                }
            }
            return null;
        }

        private void sendNotification(@NotNull Function<Notifications, Notifications.Setting> settingFunction, @NotNull ServerInfo serverInfo) {
            var setting = settingFunction.apply(ServerStatusChecker.this.configHolder.get().notifications());

            if (setting.enabled() && !setting.message().isEmpty()) {
                var formattedMessage = setting.message().replace("%server_name%", serverInfo.getName());
                ServerStatusChecker.this.webhookService.executeIfRunning(webhook -> webhook.send(formattedMessage));
            }
        }

        private enum Status {
            RUNNING,
            FIRST_FAILURE,
            STOPPED,
            UNKNOWN
        }
    }
}
