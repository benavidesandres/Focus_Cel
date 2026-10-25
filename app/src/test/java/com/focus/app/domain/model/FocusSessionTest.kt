package com.focus.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionTest {

    @Test
    fun `remainingMillis returns exact difference before expiration`() {
        val start = 1_000_000L
        val end = 2_000_000L
        val session = FocusSession(
            id = 1L,
            startTimestampMs = start,
            endTimestampMs = end,
            durationMinutes = 16,
            selectedPackageNames = setOf("com.example.app"),
            status = SessionStatus.ACTIVE
        )

        val current = 1_400_000L
        assertEquals(600_000L, session.remainingMillis(current))
    }

    @Test
    fun `remainingMillis returns zero when expired`() {
        val start = 1_000_000L
        val end = 2_000_000L
        val session = FocusSession(
            id = 1L,
            startTimestampMs = start,
            endTimestampMs = end,
            durationMinutes = 16,
            selectedPackageNames = emptySet(),
            status = SessionStatus.ACTIVE
        )

        assertEquals(0L, session.remainingMillis(2_500_000L))
    }

    @Test
    fun `isExpired returns correct boolean`() {
        val session = FocusSession(
            id = 1L,
            startTimestampMs = 1000L,
            endTimestampMs = 2000L,
            durationMinutes = 1,
            selectedPackageNames = emptySet(),
            status = SessionStatus.ACTIVE
        )

        assertFalse(session.isExpired(1500L))
        assertTrue(session.isExpired(2000L))
        assertTrue(session.isExpired(2500L))
    }

    @Test
    fun `progressFraction calculates accurate ratio`() {
        val start = 1000L
        val end = 2000L
        val session = FocusSession(
            id = 1L,
            startTimestampMs = start,
            endTimestampMs = end,
            durationMinutes = 1,
            selectedPackageNames = emptySet(),
            status = SessionStatus.ACTIVE
        )

        assertEquals(1.0f, session.progressFraction(1000L), 0.001f)
        assertEquals(0.5f, session.progressFraction(1500L), 0.001f)
        assertEquals(0.0f, session.progressFraction(2000L), 0.001f)
        assertEquals(0.0f, session.progressFraction(3000L), 0.001f)
    }
}
