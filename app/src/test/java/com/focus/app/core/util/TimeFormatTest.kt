package com.focus.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("24:30", formatCountdown(24 * 60_000L + 30_000L))
    }

    @Test
    fun `includes hours for long sessions`() {
        assertEquals("01:30:00", formatCountdown(90 * 60_000L))
    }

    @Test
    fun `rounds partial seconds up`() {
        assertEquals("00:01", formatCountdown(1L))
        assertEquals("00:00", formatCountdown(0L))
    }
}
