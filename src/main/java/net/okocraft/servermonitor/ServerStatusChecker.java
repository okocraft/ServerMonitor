package net.okocraft.servermonitor;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ServerStatusChecker implements Runnable {

    private final ProxyServer proxy;
    private final DiscordWebhookService webhookService;
    private final Map<ServerInfo, ServerStatus> statusMap = new HashMap<>();

    public ServerStatusChecker(@NotNull ProxyServer proxy, @NotNull DiscordWebhookService webhookService) {
        this.proxy = proxy;
        this.webhookService = webhookService;
    }

    @Override
    public void run() {
        for (var server : this.proxy.getAllServers()) {
            var status = this.statusMap.computeIfAbsent(server.getServerInfo(), info -> new ServerStatus(this.webhookService, info));
            status.updateStatus(server);
        }
    }
}
