package elite.kit.outwait.customDataTypes

import org.joda.time.Duration

class ReceivedList (
    val currentSlotStartedTime: Duration,
    val order: List<String>,
    val spontaneous: List<SpontaneousSlot>,
    val fixed: List<FixedSlot>
)
