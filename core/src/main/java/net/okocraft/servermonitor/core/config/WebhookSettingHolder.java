package net.okocraft.servermonitor.core.config;

import net.okocraft.servermonitor.core.discord.DiscordWebhookService;
import org.jetbrains.annotations.NotNull;

public interface WebhookSettingHolder {

    @NotNull
    String webhookUrl();

    long threadId();

    default boolean setup(@NotNull DiscordWebhookService service) {
        if (this.webhookUrl().isEmpty()) {
            return false;
        }

        service.start(this.webhookUrl(), this.threadId());
        return true;
    }
}
