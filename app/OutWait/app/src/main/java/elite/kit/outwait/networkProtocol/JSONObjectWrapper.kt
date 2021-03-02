package elite.kit.outwait.networkProtocol

import org.json.JSONObject

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
const val INSTITUTE_NAME = "instituteName"

const val ERROR_MESSAGE = "errorMessage"

const val CURRENT_SLOT_STARTED_TIME = "currentSlotStartedTime"
const val SLOT_ORDER = "slotOrder"
const val SPONTANEOUS_SLOTS = "spontaneousSlots"
const val FIXED_SLOTS = "fixedSlots"

/**
 * This is the base class for the JSONObjectWrapper
 *
 * @property jsonObj the given JSONObject that is to be wrapped
 */
abstract class JSONObjectWrapper(protected val jsonObj: JSONObject) {

    /**
     * Getter for the wrapped JSONObject
     *
     * @return JSONObject that is being wrapped
     */
    fun getJSONObject(): JSONObject {
        return jsonObj
    }

    /**
     * Getter for the wrapped JSONObject as a JSON String
     *
     * @return JSONObject that is being wrapped, as a JSON String
     */
    fun getJSONString(): String{
        return jsonObj.toString()
    }
}
