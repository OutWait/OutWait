package edu.kit.outwait.customDataTypes

import org.joda.time.DateTime

/**
 * This class bundles the waiting queue data sent by the server like specified in the
 * design document, chapter 9.1.2. Like all classes in
 * the customDataTypes it does not inherit from more general classes, so that it
 * can be serialized more easily. The main reason for this class is moving data
 * from one point to another.
 *
 * @property currentSlotStartedTime
 * @property order order of the slots as specified in the protocol
 * @property spontaneous contains all spontaneous slots which are currently in the waiting queue
 * @property fixed contains all fixed slots which are currently in the waiting queue
 */
class ReceivedList(
    val currentSlotStartedTime: DateTime,
    val order: List<String>,
    val spontaneous: List<SpontaneousSlot>,
    val fixed: List<FixedSlot>
)
