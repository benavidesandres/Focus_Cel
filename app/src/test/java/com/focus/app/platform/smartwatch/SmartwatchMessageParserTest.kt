package com.focus.app.platform.smartwatch

import com.focus.app.domain.model.FocusActivityType
import com.focus.app.domain.model.SmartwatchCommand
import com.focus.app.domain.model.SmartwatchProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartwatchMessageParserTest {

    @Test
    fun `start with parameters is parsed`() {
        val message = SmartwatchMessageParser.parse(
            SmartwatchProtocol.PATH_START_SESSION,
            """{"durationMinutes":25,"activityId":"study"}""".toByteArray()
        )

        assertEquals(
            SmartwatchMessage.Command(
                SmartwatchCommand.StartSession(25, FocusActivityType.STUDY)
            ),
            message
        )
    }

    @Test
    fun `start without payload falls back to phone defaults`() {
        val message = SmartwatchMessageParser.parse(
            SmartwatchProtocol.PATH_START_SESSION,
            ByteArray(0)
        )

        assertEquals(
            SmartwatchMessage.Command(SmartwatchCommand.StartSession(null, null)),
            message
        )
    }

    @Test
    fun `stop, movement and dismiss become commands`() {
        assertEquals(
            SmartwatchMessage.Command(SmartwatchCommand.StopSession),
            SmartwatchMessageParser.parse(SmartwatchProtocol.PATH_STOP_SESSION, ByteArray(0))
        )
        assertEquals(
            SmartwatchMessage.Command(SmartwatchCommand.MovementDetected),
            SmartwatchMessageParser.parse(SmartwatchProtocol.PATH_ACCELEROMETER_EVENT, ByteArray(0))
        )
        assertEquals(
            SmartwatchMessage.Command(SmartwatchCommand.DismissInactivityAlert),
            SmartwatchMessageParser.parse(SmartwatchProtocol.PATH_DISMISS_ALERT, ByteArray(0))
        )
    }

    @Test
    fun `status keeps only valid fields`() {
        val message = SmartwatchMessageParser.parse(
            SmartwatchProtocol.PATH_DEVICE_STATUS,
            """{"model":"Pixel Watch","battery":250}""".toByteArray()
        )

        assertEquals(SmartwatchMessage.Status("Pixel Watch", null), message)
    }

    @Test
    fun `malformed payload does not crash`() {
        val message = SmartwatchMessageParser.parse(
            SmartwatchProtocol.PATH_BATTERY,
            "not json".toByteArray()
        )

        assertEquals(SmartwatchMessage.Status(null, null), message)
    }

    @Test
    fun `unknown paths are ignored`() {
        assertNull(SmartwatchMessageParser.parse("/other/path", ByteArray(0)))
    }
}
