package net.okocraft.servermonitor.velocity.config;

public record Config(String discordWebhookUrl, long checkInterval, Notifications notifications) {

    public static final Config DEFAULT = new Config("", 300L, new Notifications(
            new Notifications.Setting(true, "Server **%server_name%** is running!"),
            new Notifications.Setting(true, "Server **%server_name%** has started!"),
            new Notifications.Setting(true, "Server **%server_name%** has stopped!"),
            new Notifications.Setting(true, "Server **%server_name%** hasn't started yet!"),
            new Notifications.Setting(true, "Cannot ping to server **%server_name%**, might it be down?")
    ));

}
