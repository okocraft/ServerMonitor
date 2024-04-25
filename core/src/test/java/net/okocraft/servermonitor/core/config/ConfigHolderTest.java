package net.okocraft.servermonitor.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigHolderTest {

    @Test
    void testLoading(@TempDir Path dir) throws IOException {
        var filepath = dir.resolve("config.yml");
        var defaultConfig = new Config(100, "test");
        var holder = new ConfigHolder<>(defaultConfig, UnaryOperator.identity());

        { // first loading (saving default config entries)
            holder.load(filepath);

            assertTrue(Files.isRegularFile(filepath));
            assertLinesMatch(defaultConfig.asYaml().lines(), Files.readString(filepath, StandardCharsets.UTF_8).lines());

            assertEquals(defaultConfig, holder.get());
        }

        { // second loading (file changed)
            var modified = new Config(150, "TEST");
            modifyAndLoadConfig(filepath, modified, holder, false);
        }

        { // reloading (file changed)
            var modified = new Config(500, "TEst");
            modifyAndLoadConfig(filepath, modified, holder, true);
        }
    }

    private static void modifyAndLoadConfig(Path filepath, Config modified, ConfigHolder<Config> holder, boolean reload) throws IOException {
        Files.writeString(filepath, modified.asYaml(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        if (reload) {
            holder.reload(filepath);
        } else {
            holder.load(filepath);
        }

        assertEquals(modified, holder.get());
    }

    private record Config(int value, String string) {
        public String asYaml() {
            return "value: " + this.value + System.lineSeparator() +
                   "string: " + this.string + System.lineSeparator();
        }
    }
}
