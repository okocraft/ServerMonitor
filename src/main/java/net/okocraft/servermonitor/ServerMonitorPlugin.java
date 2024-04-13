package net.okocraft.servermonitor;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.okocraft.servermonitor.config.Config;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class ServerMonitorPlugin {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final AtomicReference<Config> configReference;
    private final DiscordWebhookService webhookService;
    private ScheduledTask monitorTask;

    @Inject
    public ServerMonitorPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.configReference = new AtomicReference<>(Config.DEFAULT);
        this.webhookService = new DiscordWebhookService(this.configReference);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onEnable(ProxyInitializeEvent ignored) {
        this.logger.info("Loading config.yml...");

        try {
            this.configReference.set(Config.loadFromYamlFile(this.dataDirectory.resolve("config.yml")));
        } catch (IOException e) {
            this.logger.error("Could not load config.yml", e);
            return;
        }

        this.start();
        this.proxy.getCommandManager().register("smreload", new ReloadCommand());

        this.logger.info("Successfully enabled!");
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisable(ProxyShutdownEvent ignored) {
        this.proxy.getCommandManager().unregister("smreload");
        this.stop();

        this.logger.info("Successfully disabled!");
    }

    private void start() {
        var config = this.configReference.get();

        if (config.discordWebhookUrl().isEmpty()) {
            this.logger.warn("No Webhook url has been set.");
            return;
        }

        this.webhookService.start();
        this.monitorTask = this.proxy.getScheduler().buildTask(this, new ServerStatusChecker(this.proxy, this.webhookService)).repeat(Duration.ofSeconds(config.checkInterval())).schedule();
    }

    private void stop() {
        if (this.monitorTask != null) {
            this.monitorTask.cancel();
            this.monitorTask = null;
        }

        this.webhookService.shutdownIfRunning();
    }

    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            var sender = invocation.source();

            var plugin = ServerMonitorPlugin.this;

            plugin.stop();

            Config config;

            try {
                config = Config.loadFromYamlFile(plugin.dataDirectory.resolve("config.yml"));
            } catch (IOException e) {
                plugin.logger.error("Could not load config.yml", e);
                sender.sendMessage(Component.text("Failed to load config.yml. Please check the console."));
                return;
            }

            plugin.configReference.set(config);
            plugin.start();

            if (config.discordWebhookUrl().isEmpty()) {
                sender.sendMessage(Component.text("ServerMonitor has been reloaded: No Webhook url has been set."));
            } else {
                sender.sendMessage(Component.text("ServerMonitor has been reloaded!"));
            }
        }
    }
}
