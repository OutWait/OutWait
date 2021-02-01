package edu.kit.outwait.protocol

class JSONAddFixedSlotWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(object: JSONObject) {}
    fun setAppointmentTime(time: Date) {}
    fun setDuration(duration: Duration) {}
    fun getAppointmentTime(): Date {}
    fun getDuration(): Duration {}
}
