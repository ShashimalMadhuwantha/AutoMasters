package com.automasters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for App class.
 */
class AppTest {

    @Test
    void testGetGreeting() {
        App app = new App();
        assertEquals("Hello from AutoMasters!", app.getGreeting());
    }
}
