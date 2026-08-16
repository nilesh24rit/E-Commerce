package com.commercex.async;

import com.commercex.config.AsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.junit.jupiter.api.Assertions.*;

class AsyncExecutionTest {

    @Test
    void testAsyncConfiguration_Presence() {
        assertTrue(AsyncConfig.class.isAnnotationPresent(EnableAsync.class));
    }
}
