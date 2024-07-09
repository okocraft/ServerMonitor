package net.okocraft.servermonitor.core.config;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerialization;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

@SuppressWarnings("UnstableApiUsage")
public final class ConfigHolder<C extends Record> {

    private final C defaultConfig;
    private final AtomicReference<C> configRef;
    private final UnaryOperator<RecordSerialization.Builder<C>> serializationConfigurator;

    public ConfigHolder(@NotNull C defaultConfig, @NotNull UnaryOperator<RecordSerialization.Builder<C>> serializationConfigurator) {
        this.defaultConfig = defaultConfig;
        this.configRef = new AtomicReference<>(defaultConfig);
        this.serializationConfigurator = serializationConfigurator;
    }

    public @NotNull C get() {
        return this.configRef.get();
    }

    public void load(@NotNull Path filepath) throws IOException {
        var loaded = YamlFormat.COMMENT_PROCESSING.load(filepath);
        var serialization = this.createSerialization();

        if (applyDefaults(serialization.serializer().serialize(this.defaultConfig), loaded)) {
            YamlFormat.COMMENT_PROCESSING.save(loaded, filepath);
        }

        this.configRef.set(serialization.deserializer().apply(loaded));
    }

    public void reload(@NotNull Path filepath) throws IOException {
        this.configRef.set(this.createSerialization().deserializer().apply(YamlFormat.DEFAULT.load(filepath)));
    }

    private @NotNull RecordSerialization<C> createSerialization() {
        return this.serializationConfigurator.apply(RecordSerialization.builder(this.defaultConfig).keyGenerator(KeyGenerator.CAMEL_TO_KEBAB)).build();
    }

    private static boolean applyDefaults(@NotNull MapNode defaultNode, @NotNull MapNode target) {
        boolean applied = false;
        for (var defaultEntry : defaultNode.value().entrySet()) {
            if (target.containsKey(defaultEntry.getKey())) {
                if (defaultEntry.getValue() instanceof MapNode child) {
                    applied |= applyDefaults(child, target.getOrCreateMap(defaultEntry.getKey()));
                }
            } else {
                applied = true;
                target.set(defaultEntry.getKey(), defaultEntry.getValue());
            }
        }
        return applied;
    }
}
