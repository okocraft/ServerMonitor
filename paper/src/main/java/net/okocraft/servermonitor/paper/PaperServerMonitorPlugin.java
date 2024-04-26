package net.okocraft.servermonitor.paper;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.helpers.SubstituteLogger;

import static net.okocraft.servermonitor.core.util.ServerMonitorLogger.logger;

public class PaperServerMonitorPlugin extends JavaPlugin {

    public PaperServerMonitorPlugin() {
        ((SubstituteLogger) logger()).setDelegate(this.getSLF4JLogger());
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
