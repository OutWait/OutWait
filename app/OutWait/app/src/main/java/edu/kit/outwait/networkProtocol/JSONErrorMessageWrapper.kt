package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of events, where an error message is to be received
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (containing the received data of the
 * respective event)
 */
class JSONErrorMessageWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Getter for the error message contained in the received JSONObject
     *
     * @return received error message as a String
     */
    fun getErrorMessage(): String {
        return jsonObj.getString(ERROR_MESSAGE)
    }
}
