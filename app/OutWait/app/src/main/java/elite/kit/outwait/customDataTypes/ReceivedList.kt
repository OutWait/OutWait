package elite.kit.outwait.customDataTypes

import org.joda.time.DateTime

class ReceivedList(
    val currentSlotStartedTime: DateTime,
    val order: List<String>,
    val spontaneous: List<SpontaneousSlot>,
    val fixed: List<FixedSlot>
)
