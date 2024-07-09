package net.okocraft.servermonitor.velocity;

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
import net.okocraft.servermonitor.core.config.ConfigHolder;
import net.okocraft.servermonitor.core.discord.DiscordWebhookService;
import net.okocraft.servermonitor.velocity.config.Config;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.UnaryOperator;

import static net.okocraft.servermonitor.core.util.ServerMonitorLogger.logDebug;
import static net.okocraft.servermonitor.core.util.ServerMonitorLogger.logger;

public class VelocityServerMonitorPlugin {

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final ConfigHolder<Config> configHolder;
    private final DiscordWebhookService webhookService;
    private ScheduledTask monitorTask;

    @Inject
    public VelocityServerMonitorPlugin(Logger logger, ProxyServer proxy, @DataDirectory Path dataDirectory) {
        ((SubstituteLogger) logger()).setDelegate(logger);
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.configHolder = new ConfigHolder<>(Config.DEFAULT, UnaryOperator.identity());
        this.webhookService = new DiscordWebhookService();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onEnable(ProxyInitializeEvent ignored) {
        logger().info("Loading config.yml...");

        try {
            this.configHolder.load(this.dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            logger().error("Could not load config.yml", e);
            return;
        }

        logDebug(this.configHolder.get().debug());

        this.start();
        this.proxy.getCommandManager().register("smvreload", new ReloadCommand());

        logger().info("Successfully enabled!");
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisable(ProxyShutdownEvent ignored) {
        this.proxy.getCommandManager().unregister("smvreload");
        this.stop();

        logger().info("Successfully disabled!");
    }

    private void start() {
        var config = this.configHolder.get();
        var url = config.discordWebhookUrl();

        if (url.isEmpty()) {
            logger().warn("No Webhook url has been set.");
            return;
        }

        this.webhookService.start(url);
        this.monitorTask = this.proxy.getScheduler().buildTask(this, new ServerStatusChecker(this.proxy, this.configHolder, this.webhookService)).repeat(Duration.ofSeconds(config.checkInterval())).schedule();
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

            var plugin = VelocityServerMonitorPlugin.this;

            plugin.stop();

            try {
                plugin.configHolder.reload(plugin.dataDirectory.resolve("config.yml"));
            } catch (IOException e) {
                logger().error("Could not load config.yml", e);
                sender.sendMessage(Component.text("Failed to load config.yml. Please check the console."));
                return;
            }

            logDebug(plugin.configHolder.get().debug());

            plugin.start();

            if (plugin.configHolder.get().discordWebhookUrl().isEmpty()) {
                sender.sendMessage(Component.text("ServerMonitor has been reloaded: No Webhook url has been set."));
            } else {
                sender.sendMessage(Component.text("ServerMonitor has been reloaded!"));
            }
        }
    }
}
