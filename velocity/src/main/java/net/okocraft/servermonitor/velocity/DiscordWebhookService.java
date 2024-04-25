package net.okocraft.servermonitor.velocity;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.okocraft.servermonitor.core.config.ConfigHolder;
import net.okocraft.servermonitor.core.util.NamedThreadFactory;
import net.okocraft.servermonitor.velocity.config.Config;
import net.okocraft.servermonitor.velocity.config.Notifications;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DiscordWebhookService {

    private final ConfigHolder<Config> configReference;
    private WebhookClient webhook;

    public DiscordWebhookService(@NotNull ConfigHolder<Config> configReference) {
        this.configReference = configReference;
    }

    public boolean isRunning() {
        return this.webhook != null && !this.webhook.isShutdown();
    }

    public void start() {
        this.shutdownIfRunning();
        this.webhook =
                new WebhookClientBuilder(this.configReference.get().discordWebhookUrl())
                        .setThreadFactory(NamedThreadFactory.DEFAULT)
                        .setWait(true).build();
    }

    public void shutdownIfRunning() {
        if (this.isRunning()) {
            this.webhook.close();
            this.webhook = null;
        }
    }

    public void sendNotification(@NotNull Function<Notifications, Notifications.Setting> settingFunction, @NotNull ServerInfo serverInfo) {
        var setting = settingFunction.apply(this.configReference.get().notifications());
        if (setting.enabled() && this.isRunning() && !setting.message().isEmpty()) {
            this.webhook.send(
                    setting.message().replace("%server_name%", serverInfo.getName())
            );
        }
    }
}
