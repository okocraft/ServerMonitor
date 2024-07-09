package net.okocraft.servermonitor.core.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import net.okocraft.servermonitor.core.util.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DiscordWebhookService {

    private final StampedLock lock = new StampedLock();
    private volatile WebhookClient webhook;

    public void start(@NotNull String url, long threadId) {
        long stamp = this.lock.writeLock();

        try {
            this.shutdownIfRunningAtUnsyncronized();
            this.webhook = new WebhookClientBuilder(url).setThreadId(threadId).setThreadFactory(NamedThreadFactory.DEFAULT).setWait(true).build();
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    public void shutdownIfRunning() {
        long stamp = this.lock.writeLock();

        try {
            this.shutdownIfRunningAtUnsyncronized();
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    public void sendIfRunning(@NotNull Supplier<String> supplier) {
        this.executeIfRunning(webhook -> webhook.send(supplier.get()));
    }

    public void executeIfRunning(@NotNull Consumer<WebhookClient> action) {
        long stamp = this.lock.readLock();

        try {
            var webhook = this.webhook;

            if (webhook != null && !webhook.isShutdown()) {
                action.accept(webhook);
            }
        } finally {
            this.lock.unlockRead(stamp);
        }
    }

    private void shutdownIfRunningAtUnsyncronized() {
        if (this.webhook != null && !this.webhook.isShutdown()) {
            this.webhook.close();
            this.webhook = null;
        }
    }
}
