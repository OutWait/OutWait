package elite.kit.outwait.networkProtocol

import org.json.JSONObject

//TODO move Events in protocol package?

/*
Die JSON Keys, welche im Transportprotokoll spezifiziert wurden
 */

const val SLOT_CODE = "slotCode"
const val USERNAME = "username"
const val PASSWORD= "password"

const val DURATION= "duration"
const val TIME_OF_CREATION = "timeOfCreation"
const val APPOINTMENT_TIME = "appointmentTime"
const val MOVED_SLOT = "movedSlot"
const val OTHER_SLOT = "otherSlot"

const val NEW_TIME = "newTime"
const val NEW_DURATION = "newDuration"

const val PRIORITIZATION_TIME = "prioritizationTime"
const val MODE = "mode"
const val DELAY_NOTIFICATION_TIME = "delayNotificationTime"
const val NOTIFICATION_TIME = "notificationTime"
const val DEFAULT_SLOT_DURATION = "defaultSlotDuration"

const val APPROX_TIME = "approxTime"
const val NAME = "name"

const val ERROR_MESSAGE = "errorMessage"



abstract class JSONObjectWrapper(protected val jsonObj: JSONObject) {

    fun getJSONObject(): JSONObject {
        return jsonObj
    }

    fun getJSONString(): String{
        return jsonObj.toString()
    }
}
