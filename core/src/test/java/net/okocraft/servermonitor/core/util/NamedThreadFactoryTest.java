package net.okocraft.servermonitor.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedThreadFactoryTest {
    @Test
    void testNextName() {
        assertEquals("ServerMonitor-Thread-1", NamedThreadFactory.createDefault().nextName());
    }
}
