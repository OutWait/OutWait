package elite.kit.outwait.customDataTypes

import org.joda.time.DateTime
import org.joda.time.Duration

class FixedSlot(
    val duration: Duration,
    val slotCode: String,
    val appointmentTime: DateTime
)
