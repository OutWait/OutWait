package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver

class UpdateMediatorMock {
    val receivers = mutableListOf<SlotInformationReceiver>()

    fun subscribeReceiver(slot: SlotInformationReceiver) {
        receivers.add(slot)
    }

    fun unsubscribeReceiver(slot: SlotInformationReceiver) {
        receivers.remove(slot)
    }
}
