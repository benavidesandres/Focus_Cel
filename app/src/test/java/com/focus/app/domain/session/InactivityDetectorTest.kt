package com.focus.app.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InactivityDetectorTest {

    private val detector = InactivityDetector()

    @Test
    fun `evaluate does nothing when not monitoring`() {
        assertFalse(detector.evaluate(nowMs = 999_999_999L))
    }

    @Test
    fun `alert fires once when the threshold is reached`() {
        detector.startMonitoring(thresholdMinutes = 45, nowMs = 0L)

        assertFalse(detector.evaluate(nowMs = 44 * MINUTE))
        assertTrue(detector.evaluate(nowMs = 45 * MINUTE))
        assertFalse(detector.evaluate(nowMs = 46 * MINUTE))
        assertTrue(detector.status.value.isAlertActive)
        assertEquals(46, detector.status.value.minutesWithoutMovement)
    }

    @Test
    fun `movement clears the alert and restarts the measurement`() {
        detector.startMonitoring(thresholdMinutes = 30, nowMs = 0L)
        detector.evaluate(nowMs = 30 * MINUTE)

        detector.onMovementDetected(nowMs = 31 * MINUTE)

        assertFalse(detector.status.value.isAlertActive)
        assertFalse(detector.evaluate(nowMs = 60 * MINUTE))
        assertTrue(detector.evaluate(nowMs = 61 * MINUTE))
    }

    @Test
    fun `stopMonitoring resets the status`() {
        detector.startMonitoring(thresholdMinutes = 30, nowMs = 0L)
        detector.evaluate(nowMs = 30 * MINUTE)

        detector.stopMonitoring()

        assertEquals(InactivityStatus(), detector.status.value)
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
