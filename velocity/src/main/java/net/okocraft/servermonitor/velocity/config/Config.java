package net.okocraft.servermonitor.velocity.config;

import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerialization;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
public record Config(String discordWebhookUrl, long checkInterval, Notifications notifications) {

    public static final Config DEFAULT = new Config("", 300L, new Notifications(
            new Notifications.Setting(true, "Server **%server_name%** is running!"),
            new Notifications.Setting(true, "Server **%server_name%** has started!"),
            new Notifications.Setting(true, "Server **%server_name%** has stopped!"),
            new Notifications.Setting(true, "Server **%server_name%** hasn't started yet!"),
            new Notifications.Setting(true, "Cannot ping to server **%server_name%**, might it be down?")
    ));

    private static final RecordSerialization<Config> SERIALIZATION = RecordSerialization.builder(DEFAULT).keyGenerator(KeyGenerator.CAMEL_TO_KEBAB).build();

    public static Config loadFromYamlFile(Path filepath) throws IOException {
        if (Files.isRegularFile(filepath)) {
            return SERIALIZATION.deserializer().deserialize(YamlFormat.DEFAULT.load(filepath));
        } else {
            YamlFormat.COMMENT_PROCESSING.save(SERIALIZATION.serializer().serialize(DEFAULT), filepath);
            return DEFAULT;
        }
    }
}
