package net.flex.ManualTournaments;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private ServerMock server;
    private Main plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Main.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginLoads() {
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }

    @Test
    void pluginName() {
        assertEquals("ManualTournaments", plugin.getName());
    }
}
